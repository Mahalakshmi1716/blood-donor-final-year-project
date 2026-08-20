package com.example.blood_donor.network

import com.example.blood_donor.data.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST



interface ApiService {
    // --- Auth ---
    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @GET("api/auth/me")
    suspend fun getMe(): Response<AuthResponse>

    @POST("api/auth/update")
    suspend fun updateProfile(@Body request: UpdateProfileRequest): Response<AuthResponse>

    @POST("api/auth/forgot-password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest): Response<AuthResponse>

    @POST("api/auth/reset-password")
    suspend fun resetPassword(@Body request: ResetPasswordRequest): Response<AuthResponse>

    // --- Donor ---
    @POST("api/donors/profile")
    suspend fun createDonorProfile(@Body request: DonorProfileRequest): Response<DonorProfileResponse>

    @GET("api/donors/profile")
    suspend fun getDonorProfile(): Response<DonorProfileResponse>

    @POST("api/donors/availability")
    suspend fun updateAvailability(@Body request: AvailabilityRequest): Response<DonorProfileResponse>

    @POST("api/donors/record-donation")
    suspend fun recordDonation(@Body request: RecordDonationRequest): Response<RecordDonationResponse>

    @GET("api/donors/tip-of-the-day")
    suspend fun getTipOfTheDay(): Response<TipResponse>

    // --- Patient ---
    @POST("api/patients/search")
    suspend fun searchDonors(@Body request: SearchRequest): Response<SearchResponse>

    @POST("api/patients/sos")
    suspend fun triggerSos(@Body request: SosRequest): Response<SosResponse>

    @GET("api/patients/alerts")
    suspend fun getAlerts(): Response<AlertsResponse>

    @POST("api/patients/alerts/{id}/accept")
    suspend fun acceptAlert(@retrofit2.http.Path("id") id: Int): Response<SosResponse>

    @POST("api/patients/alerts/{id}/decline")
    suspend fun declineAlert(@retrofit2.http.Path("id") id: Int): Response<Void>

    @POST("api/patients/alerts/{id}/start-travel")
    suspend fun startTravel(@retrofit2.http.Path("id") id: Int): Response<SosResponse>

    @POST("api/patients/alerts/{id}/start-donation")
    suspend fun startDonation(@retrofit2.http.Path("id") id: Int): Response<SosResponse>

    @POST("api/patients/alerts/{id}/confirm-donation")
    suspend fun confirmDonation(@retrofit2.http.Path("id") id: Int): Response<CertificateResponse>

    @POST("api/patients/alerts/{id}/cancel")
    suspend fun cancelAlert(@retrofit2.http.Path("id") id: Int, @Body body: CancelRequest): Response<SosResponse>

    @GET("api/patients/hospital-analytics")
    suspend fun getHospitalAnalytics(): Response<HospitalAnalyticsResponse>

    @GET("api/patients/analytics")
    suspend fun getPatientAnalytics(): Response<PatientAnalyticsResponse>

    // --- OTP Verify ---
    @POST("api/auth/verify-email-otp")
    suspend fun verifyOtp(@Body request: VerifyOtpRequest): Response<AuthResponse>

    // --- Chat ---
    @POST("api/chat/send")
    suspend fun sendMessage(@Body request: SendMessageRequest): Response<SendMessageResponse>

    @GET("api/chat/history/{userId}")
    suspend fun getChatHistory(@retrofit2.http.Path("userId") userId: Int): Response<ChatHistoryResponse>

    @GET("api/chat/conversations")
    suspend fun getConversations(): Response<ConversationsResponse>
}

