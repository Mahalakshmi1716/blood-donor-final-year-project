package com.example.blood_donor.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.blood_donor.data.LoginRequest
import com.example.blood_donor.data.RegisterRequest
import com.example.blood_donor.data.TokenManager
import com.example.blood_donor.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.example.blood_donor.data.DonorProfileRequest



sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val message: String) : AuthState()
    data class Unverified(val email: String) : AuthState()
    data class Error(val error: String) : AuthState()
}

class AuthViewModel(val tokenManager: TokenManager) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    fun login(phoneNumber: String, pass: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val response = RetrofitClient.apiService.login(LoginRequest(phoneNumber, pass))
                if (response.isSuccessful && response.body() != null) {
                    val token = response.body()?.token
                    if (token != null) {
                        tokenManager.saveToken(token)
                        _authState.value = AuthState.Success("Login Successful")
                    } else {
                        _authState.value = AuthState.Error("Token missing in response")
                    }
                } else {
                    if (response.code() == 403) {
                        val errorBody = response.errorBody()?.string()
                        val gson = com.google.gson.Gson()
                        val errorResponse = try { gson.fromJson(errorBody, com.example.blood_donor.data.AuthResponse::class.java) } catch(e: Exception) { null }
                        if (errorResponse?.unverified == true) {
                            val email = errorResponse.email ?: errorResponse.user?.email ?: phoneNumber
                            _authState.value = AuthState.Unverified(email)
                            return@launch
                        }
                    }
                    val errKey = when (response.code()) {
                        401 -> "error_invalid_password"
                        404 -> "error_not_registered"
                        else -> "error_server_error"
                    }
                    _authState.value = AuthState.Error(errKey)
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error("error_network_unavailable")
            }
        }
    }

    fun register(name: String, email: String, phoneNumber: String, pass: String, bloodGroup: String?, age: Int?, gender: String?, userType: String, lastDonationDate: String? = null) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val response = RetrofitClient.apiService.register(RegisterRequest(name, email, phoneNumber, pass, userType, bloodGroup, age, gender, lastDonationDate))
                if (response.isSuccessful && response.body() != null) {
                    val registerBody = response.body()
                    if (registerBody?.requiresVerification == true) {
                        _authState.value = AuthState.Unverified(email)
                    } else {
                        // Auto login after registration if not requiring verification
                        val loginResponse = RetrofitClient.apiService.login(LoginRequest(phoneNumber, pass))
                        if (loginResponse.isSuccessful && loginResponse.body() != null) {
                            val token = loginResponse.body()?.token
                            if (token != null) {
                                tokenManager.saveToken(token)
                                if (bloodGroup != null) {
                                    try {
                                        RetrofitClient.apiService.createDonorProfile(DonorProfileRequest(bloodGroup, age, gender, lastDonationDate))
                                    } catch (e: Exception) {}
                                }
                                _authState.value = AuthState.Success("Registration Successful")
                            } else {
                                _authState.value = AuthState.Error("Token missing in login response")
                            }
                        } else {
                            _authState.value = AuthState.Success("Registration Successful. Please Login.")
                        }
                    }
                } else {
                    _authState.value = AuthState.Error("Registration failed: ${response.code()}")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun verifyOtp(email: String, otpCode: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val response = RetrofitClient.apiService.verifyOtp(com.example.blood_donor.data.VerifyOtpRequest(email, otpCode))
                if (response.isSuccessful && response.body() != null) {
                    val token = response.body()?.token
                    if (token != null) {
                        tokenManager.saveToken(token)
                    }
                    tokenManager.saveRememberedPhone(email)
                    _authState.value = AuthState.Success("Verification Successful")
                } else {
                    _authState.value = AuthState.Error("error_invalid_credentials")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error("error_network_unavailable")
            }
        }
    }
    
    fun logout() {
        viewModelScope.launch {
            tokenManager.clearToken()
            _authState.value = AuthState.Idle
        }
    }

    fun createDonorProfile(bloodGroup: String) {
        viewModelScope.launch {
            try {
                RetrofitClient.apiService.createDonorProfile(com.example.blood_donor.data.DonorProfileRequest(bloodGroup, null, null))
            } catch (e: Exception) {
                // handle
            }
        }
    }
    
    fun forgotPassword(phoneNumber: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.forgotPassword(com.example.blood_donor.data.ForgotPasswordRequest(phoneNumber))
                if (response.isSuccessful) {
                    onSuccess()
                } else {
                    onError("Failed to send OTP: ${response.code()}")
                }
            } catch (e: Exception) {
                onError(e.message ?: "Unknown error occurred")
            }
        }
    }

    fun resetPassword(phoneNumber: String, otpCode: String, newPass: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.resetPassword(
                    com.example.blood_donor.data.ResetPasswordRequest(phoneNumber, otpCode, newPass)
                )
                if (response.isSuccessful) {
                    onSuccess()
                } else {
                    onError("Reset failed: ${response.code()}")
                }
            } catch (e: Exception) {
                onError(e.message ?: "Unknown error occurred")
            }
        }
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }
}
