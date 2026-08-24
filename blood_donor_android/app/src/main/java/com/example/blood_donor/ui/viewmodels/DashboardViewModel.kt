package com.example.blood_donor.ui.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.blood_donor.data.AlertDto
import com.example.blood_donor.data.DonorProfileDto
import com.example.blood_donor.data.MatchedDonorDto
import com.example.blood_donor.data.SearchRequest
import com.example.blood_donor.data.SosRequest
import com.example.blood_donor.network.RetrofitClient
import com.example.blood_donor.utils.NotificationHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class DashboardState {
    object Idle : DashboardState()
    object Loading : DashboardState()
    data class Success(val message: String, val prefilledMessage: String = "") : DashboardState()
    data class Error(val error: String) : DashboardState()
}

class DashboardViewModel : ViewModel() {

    private val _dashboardState = MutableStateFlow<DashboardState>(DashboardState.Idle)
    val dashboardState: StateFlow<DashboardState> = _dashboardState.asStateFlow()

    private val _profile = MutableStateFlow<DonorProfileDto?>(null)
    val profile: StateFlow<DonorProfileDto?> = _profile.asStateFlow()

    private val _user = MutableStateFlow<com.example.blood_donor.data.UserDto?>(null)
    val user: StateFlow<com.example.blood_donor.data.UserDto?> = _user.asStateFlow()
    
    private val _sosDonors = MutableStateFlow<List<MatchedDonorDto>>(emptyList())
    val sosDonors: StateFlow<List<MatchedDonorDto>> = _sosDonors.asStateFlow()

    private val _nearbyDonors = MutableStateFlow<List<MatchedDonorDto>>(emptyList())
    val nearbyDonors: StateFlow<List<MatchedDonorDto>> = _nearbyDonors.asStateFlow()

    private val _fallbackActivated = MutableStateFlow<Boolean>(false)
    val fallbackActivated: StateFlow<Boolean> = _fallbackActivated.asStateFlow()

    private val _fallbackBloodBanks = MutableStateFlow<List<com.example.blood_donor.data.BloodBankDto>>(emptyList())
    val fallbackBloodBanks: StateFlow<List<com.example.blood_donor.data.BloodBankDto>> = _fallbackBloodBanks.asStateFlow()

    fun fetchProfile() {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.getDonorProfile()
                if (response.isSuccessful) {
                    _profile.value = response.body()?.profile
                } else {
                    _profile.value = null
                }
            } catch (e: Exception) {
                _profile.value = null
            }
        }
    }

    fun fetchMe() {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.getMe()
                if (response.isSuccessful && response.body() != null) {
                    _user.value = response.body()?.user
                } else {
                    _user.value = null
                }
            } catch (e: Exception) {
                _user.value = null
            }
        }
    }

    fun updateProfile(bloodGroup: String, age: Int?, gender: String?) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.updateProfile(com.example.blood_donor.data.UpdateProfileRequest(bloodGroup, age, gender))
                if (response.isSuccessful && response.body() != null) {
                    _user.value = response.body()?.user
                }
            } catch (e: Exception) {}
        }
    }

    fun updateAvailability(
        isAvailableToday: Boolean, 
        lat: Double? = null, 
        lon: Double? = null, 
        state: String? = null, 
        district: String? = null, 
        city: String? = null,
        onComplete: () -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.updateAvailability(
                    com.example.blood_donor.data.AvailabilityRequest(isAvailableToday, lat, lon, state, district, city)
                )
                if (response.isSuccessful) {
                    _profile.value = response.body()?.profile
                    onComplete()
                }
            } catch (e: Exception) {}
        }
    }

    fun recordDonation(hospitalName: String?, location: String?, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.recordDonation(
                    com.example.blood_donor.data.RecordDonationRequest(hospitalName, location)
                )
                if (response.isSuccessful) {
                    fetchProfile()
                    onComplete()
                }
            } catch (e: Exception) {}
        }
    }

    private val _alerts = MutableStateFlow<List<com.example.blood_donor.data.AlertDto>>(emptyList())
    val alerts: StateFlow<List<com.example.blood_donor.data.AlertDto>> = _alerts.asStateFlow()

    private val _tipOfTheDay = MutableStateFlow<String>("Stay hydrated and healthy!")
    val tipOfTheDay: StateFlow<String> = _tipOfTheDay.asStateFlow()

    fun fetchTipOfTheDay() {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.getTipOfTheDay()
                if (response.isSuccessful && response.body() != null) {
                    _tipOfTheDay.value = response.body()?.tip ?: "Stay hydrated and healthy!"
                }
            } catch (e: Exception) {
                // Ignore failure, keep default
            }
        }
    }

    private val seenAlertIds = mutableSetOf<Int>()
    private var isPolling = false

    fun startAlertPolling(context: Context) {
        if (isPolling) return
        isPolling = true
        viewModelScope.launch {
            while (true) {
                try {
                    val response = RetrofitClient.apiService.getAlerts()
                    if (response.isSuccessful && response.body() != null) {
                        val fetchedAlerts = response.body()?.alerts ?: emptyList()
                        _alerts.value = fetchedAlerts

                        // Check for new alerts to notify Donors
                        if (user.value?.userType == "Donor") {
                            fetchedAlerts.forEach { alert ->
                                val alertId = alert.id ?: 0
                                if (alertId != 0 && !seenAlertIds.contains(alertId)) {
                                    seenAlertIds.add(alertId)
                                    // Trigger notification for new alert
                                    NotificationHelper.showEmergencyNotification(context, alert)
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    // Ignore failure
                }
                delay(10000) // Poll every 10 seconds
            }
        }
    }

    fun fetchAlerts() {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.getAlerts()
                if (response.isSuccessful && response.body() != null) {
                    val fetchedAlerts = response.body()?.alerts ?: emptyList()
                    _alerts.value = fetchedAlerts
                    fetchedAlerts.forEach { alert -> alert.id?.let { seenAlertIds.add(it) } }
                }
            } catch (e: Exception) {
                // Ignore failure
            }
        }
    }

    fun acceptAlert(alertId: Int, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.acceptAlert(alertId)
                if (response.isSuccessful) {
                    fetchAlerts()
                    onComplete()
                }
            } catch (e: Exception) {}
        }
    }

    fun declineAlert(alertId: Int, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.declineAlert(alertId)
                if (response.isSuccessful) {
                    fetchAlerts()
                    onComplete()
                }
            } catch (e: Exception) {}
        }
    }

    fun fetchNearbyDonors(bloodGroup: String, lat: Double, lon: Double, urgency: String = "High") {
        viewModelScope.launch {
            try {
                val request = SearchRequest(bloodGroup, lat, lon, urgency)
                val response = RetrofitClient.apiService.searchDonors(request)
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()
                    _nearbyDonors.value = body?.donors ?: emptyList()
                    _fallbackActivated.value = body?.fallbackActivated ?: false
                    _fallbackBloodBanks.value = body?.fallbackBloodBanks ?: emptyList()
                }
            } catch (e: Exception) {
                // Handle or ignore silently
            }
        }
    }

    fun createBloodRequest(bloodGroup: String, hospitalName: String, lat: Double, lon: Double, urgency: String = "High", units: Int = 1) {
        viewModelScope.launch {
            _dashboardState.value = DashboardState.Loading
            try {
                val request = SosRequest(
                    bloodGroup = bloodGroup,
                    hospitalName = hospitalName,
                    latitude = lat,
                    longitude = lon,
                    urgency = urgency,
                    unitsRequired = units
                )
                val response = RetrofitClient.apiService.triggerSos(request)
                
                if (response.isSuccessful && response.body() != null) {
                    _sosDonors.value = response.body()?.suggestedDonors ?: emptyList()
                    val msg = response.body()?.prefilledMessage ?: ""
                    _dashboardState.value = DashboardState.Success("SOS Alerts Sent Successfully!", msg)
                } else {
                    _dashboardState.value = DashboardState.Error("Failed to trigger SOS")
                }
            } catch (e: Exception) {
                _dashboardState.value = DashboardState.Error(e.message ?: "Network error")
            }
        }
    }
    
    fun resetState() {
        _dashboardState.value = DashboardState.Idle
     }

    fun clearState() {
        _profile.value = null
        _user.value = null
        _sosDonors.value = emptyList()
        _nearbyDonors.value = emptyList()
        _fallbackActivated.value = false
        _fallbackBloodBanks.value = emptyList()
        _dashboardState.value = DashboardState.Idle
    }
}
