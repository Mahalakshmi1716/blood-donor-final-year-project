import os

models_kt = r"d:\blood_donor\blood_donor_android\app\src\main\java\com\example\blood_donor\data\Models.kt"
api_service_kt = r"d:\blood_donor\blood_donor_android\app\src\main\java\com\example\blood_donor\network\ApiService.kt"

# Append Chat Models
chat_models = """

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
"""

with open(models_kt, "a", encoding="utf-8") as f:
    f.write(chat_models)

# Append Chat API Endpoints
with open(api_service_kt, "r", encoding="utf-8") as f:
    content = f.read()

chat_api = """
    // --- Chat ---
    @POST("api/chat/send")
    suspend fun sendMessage(@Body request: SendMessageRequest): Response<SendMessageResponse>

    @GET("api/chat/history/{userId}")
    suspend fun getChatHistory(@retrofit2.http.Path("userId") userId: Int): Response<ChatHistoryResponse>

    @GET("api/chat/conversations")
    suspend fun getConversations(): Response<ConversationsResponse>
}
"""
content = content.replace("}", chat_api)

with open(api_service_kt, "w", encoding="utf-8") as f:
    f.write(content)
