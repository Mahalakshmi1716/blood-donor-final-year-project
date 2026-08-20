package com.example.blood_donor.data

import com.google.gson.annotations.SerializedName

// --- Auth Models ---

data class RegisterRequest(
    val name: String,
    val email: String,
    @SerializedName("phone_number") val phoneNumber: String,
    val password: String,
    @SerializedName("user_type") val userType: String,
    @SerializedName("blood_group") val bloodGroup: String?,
    val age: Int?,
    val gender: String?,
    @SerializedName("last_donation_date") val lastDonationDate: String? = null
)

data class UpdateProfileRequest(
    @SerializedName("blood_group") val bloodGroup: String?,
    val age: Int?,
    val gender: String?
)

data class LoginRequest(
    @SerializedName("phone_number") val phoneNumber: String,
    val password: String
)

data class AuthResponse(
    val message: String?,
    val token: String?,
    val user: UserDto?,
    val email: String? = null,
    @SerializedName("unverified") val unverified: Boolean? = false,
    @SerializedName("requires_verification") val requiresVerification: Boolean? = false
)

data class VerifyOtpRequest(
    val email: String,
    @SerializedName("otp_code") val otpCode: String
)

data class UserDto(
    val id: Int,
    val name: String,
    val email: String?,
    @SerializedName("phone_number") val phoneNumber: String,
    @SerializedName("user_type") val userType: String,
    @SerializedName("blood_group") val bloodGroup: String?,
    val age: Int?,
    val gender: String?,
    @SerializedName("verified_mobile") val verifiedMobile: Boolean? = false,
    @SerializedName("verified_email") val verifiedEmail: Boolean? = false,
    @SerializedName("verification_status") val verificationStatus: String? = "Unverified",
    @SerializedName("hospital_verification_status") val hospitalVerificationStatus: String? = "Pending Verification",
    @SerializedName("hospital_license") val hospitalLicense: String? = null,
    @SerializedName("registered_address") val registeredAddress: String? = null,
    @SerializedName("preferred_language") val preferredLanguage: String? = "en"
)

data class ForgotPasswordRequest(
    @SerializedName("phone_number") val phoneNumber: String
)

data class ResetPasswordRequest(
    @SerializedName("phone_number") val phoneNumber: String,
    @SerializedName("otp_code") val otpCode: String,
    @SerializedName("new_password") val newPassword: String
)

// --- Donor Models ---

data class DonorProfileRequest(
    @SerializedName("blood_group") val bloodGroup: String,
    val age: Int?,
    val gender: String?,
    @SerializedName("last_donation_date") val lastDonationDate: String? = null
)

data class AvailabilityRequest(
    @SerializedName("is_available_today") val isAvailableToday: Boolean?,
    val latitude: Double?,
    val longitude: Double?,
    val state: String? = null,
    val district: String? = null,
    val city: String? = null
)

data class DonorProfileResponse(
    val message: String?,
    val profile: DonorProfileDto?
)

data class DonationRecordDto(
    val id: Int,
    @SerializedName("donor_id") val donorId: Int,
    @SerializedName("donation_date") val donationDate: String,
    @SerializedName("hospital_name") val hospitalName: String?,
    val location: String?
)

data class DonorProfileDto(
    val id: Int,
    val name: String?,
    @SerializedName("phone_number") val phoneNumber: String?,
    @SerializedName("blood_group") val bloodGroup: String,
    val age: Int?,
    val gender: String?,
    @SerializedName("health_score") val healthScore: Int?,
    @SerializedName("donation_count") val donationCount: Int?,
    @SerializedName("trust_score") val trustScore: Double?,
    @SerializedName("is_available_today") val isAvailableToday: Boolean,
    @SerializedName("last_donation_date") val lastDonationDate: String?,
    val latitude: Double?,
    val longitude: Double?,
    val state: String? = null,
    val district: String? = null,
    val city: String? = null,
    val donations: List<DonationRecordDto>? = null,
    @SerializedName("eligibility_status") val eligibilityStatus: String? = "ELIGIBLE",
    @SerializedName("total_requests_received") val totalRequestsReceived: Int? = 0,
    @SerializedName("total_requests_accepted") val totalRequestsAccepted: Int? = 0,
    @SerializedName("total_requests_rejected") val totalRequestsRejected: Int? = 0,
    @SerializedName("total_requests_ignored") val totalRequestsIgnored: Int? = 0,
    @SerializedName("response_time_average") val responseTimeAverage: Double? = 0.0,
    @SerializedName("cancellation_count") val cancellationCount: Int? = 0
)

data class RecordDonationRequest(
    @SerializedName("hospital_name") val hospitalName: String?,
    val location: String?
)

data class RecordDonationResponse(
    val message: String,
    @SerializedName("last_donation_date") val lastDonationDate: String
)

// --- Fallback & Support Models ---
data class BloodBankDto(
    val id: Int,
    @SerializedName("blood_bank_name") val bloodBankName: String,
    val location: String,
    val latitude: Double?,
    val longitude: Double?,
    @SerializedName("contact_number") val contactNumber: String,
    @SerializedName("availability_status") val availabilityStatus: String
)

data class DigitalCertificateDto(
    val id: Int,
    @SerializedName("certificate_id") val certificateId: String,
    @SerializedName("donor_id") val donorId: Int,
    @SerializedName("donor_name") val donorName: String,
    @SerializedName("hospital_name") val hospitalName: String,
    @SerializedName("donation_date") val donationDate: String,
    @SerializedName("blood_group") val bloodGroup: String,
    @SerializedName("qr_code_content") val qrCodeContent: String
)

// --- Patient / SOS Models ---

data class SearchRequest(
    @SerializedName("blood_group") val bloodGroup: String,
    val latitude: Double,
    val longitude: Double,
    val urgency: String = "High"
)

data class SearchResponse(
    val donors: List<MatchedDonorDto>,
    @SerializedName("fallback_activated") val fallbackActivated: Boolean? = false,
    @SerializedName("fallback_blood_banks") val fallbackBloodBanks: List<BloodBankDto>? = null
)

data class SosRequest(
    @SerializedName("blood_group") val bloodGroup: String,
    @SerializedName("hospital_name") val hospitalName: String,
    val latitude: Double,
    val longitude: Double,
    val urgency: String = "High",
    @SerializedName("units_required") val unitsRequired: Int = 1
)

data class SosResponse(
    val message: String,
    @SerializedName("prefilled_message") val prefilledMessage: String,
    @SerializedName("suggested_donors") val suggestedDonors: List<MatchedDonorDto>,
    @SerializedName("fallback_activated") val fallbackActivated: Boolean? = false,
    @SerializedName("fallback_blood_banks") val fallbackBloodBanks: List<BloodBankDto>? = null,
    val alert: AlertDto? = null
)

data class MatchedDonorDto(
    @SerializedName("donor_id") val donorId: Int,
    val name: String,
    @SerializedName("blood_group") val bloodGroup: String,
    @SerializedName("phone_number") val phoneNumber: String,
    val distance_km: Double,
    val health_score: Int,
    val final_score: Double,
    @SerializedName("response_rate") val responseRate: Double? = 0.95,
    @SerializedName("is_exact_match") val isExactMatch: Boolean = true,
    @SerializedName("duration_mins") val durationMins: Double? = 10.0,
    @SerializedName("match_explanation") val matchExplanation: String? = null,
    @SerializedName("compatible_match") val compatibleMatch: Boolean? = false,
    @SerializedName("exact_match") val exactMatch: Boolean? = true,
    @SerializedName("predicted_response_probability") val predictedResponseProbability: Double? = 0.95,
    @SerializedName("predicted_acceptance_probability") val predictedAcceptanceProbability: Double? = 0.95,
    @SerializedName("predicted_availability_score") val predictedAvailabilityScore: Double? = 0.95,
    @SerializedName("ai_confidence_score") val aiConfidenceScore: Double? = 0.95,
    @SerializedName("match_score") val matchScore: Double? = 80.0,
    @SerializedName("blood_group_score") val bloodGroupScore: Double? = 40.0,
    @SerializedName("distance_score") val distanceScore: Double? = 25.0,
    @SerializedName("availability_score") val availabilityScore: Double? = 15.0,
    @SerializedName("response_score") val responseScore: Double? = 5.0,
    @SerializedName("eligibility_score") val eligibilityScore: Double? = 10.0,
    @SerializedName("verification_score") val verificationScore: Double? = 5.0,
    @SerializedName("match_reason") val matchReason: String? = null,
    @SerializedName("trust_score") val trustScore: Double? = 75.0,
    val latitude: Double? = null,
    val longitude: Double? = null
)

data class TipResponse(
    val tip: String
)

// --- Chat Models ---
data class MessageDto(
    val id: Int,
    @SerializedName("sender_id") val senderId: Int,
    @SerializedName("receiver_id") val receiverId: Int,
    @SerializedName("sender_name") val senderName: String?,
    @SerializedName("receiver_name") val receiverName: String?,
    val content: String,
    val timestamp: String
)

data class SendMessageRequest(
    @SerializedName("receiver_id") val receiverId: Int,
    val content: String
)

data class SendMessageResponse(
    val message: String,
    val data: MessageDto
)

data class ChatHistoryResponse(
    val messages: List<MessageDto>
)

data class ConversationDto(
    @SerializedName("user_id") val userId: Int,
    val name: String,
    @SerializedName("phone_number") val phoneNumber: String,
    @SerializedName("last_message") val lastMessage: String,
    @SerializedName("last_message_time") val lastMessageTime: String?
)

data class ConversationsResponse(
    val conversations: List<ConversationDto>
)

// --- Alerts Models ---
data class AlertDto(
    val id: Int,
    @SerializedName("patient_id") val patientId: Int,
    @SerializedName("patient_name") val patientName: String,
    @SerializedName("blood_group") val bloodGroup: String,
    @SerializedName("hospital_name") val hospitalName: String,
    val latitude: Double?,
    val longitude: Double?,
    val status: String,
    val urgency: String,
    @SerializedName("units_required") val unitsRequired: Int,
    val timestamp: String
)

data class AlertsResponse(
    val alerts: List<AlertDto>
)

data class CancelRequest(
    val reason: String
)

data class CertificateResponse(
    val message: String?,
    val certificate: DigitalCertificateDto?
)

data class HospitalAnalyticsResponse(
    @SerializedName("total_requests") val totalRequests: Int,
    @SerializedName("active_requests") val activeRequests: Int,
    @SerializedName("completed_requests") val completedRequests: Int,
    @SerializedName("expired_requests") val expiredRequests: Int,
    @SerializedName("cancelled_requests") val cancelledRequests: Int,
    @SerializedName("success_rate") val successRate: Int,
    @SerializedName("average_match_time_mins") val averageMatchTimeMins: Double,
    @SerializedName("average_response_time_mins") val averageResponseTimeMins: Double,
    @SerializedName("critical_requests_count") val criticalRequestsCount: Int
)

data class PatientAnalyticsResponse(
    @SerializedName("total_requests") val totalRequests: Int,
    @SerializedName("completed_requests") val completedRequests: Int,
    @SerializedName("pending_requests") val pendingRequests: Int,
    @SerializedName("average_match_time_mins") val averageMatchTimeMins: Double,
    val history: List<AlertDto>? = null
)
