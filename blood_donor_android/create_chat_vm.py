import os

path = r"d:\blood_donor\blood_donor_android\app\src\main\java\com\example\blood_donor\ui\viewmodels\ChatViewModel.kt"

content = """package com.example.blood_donor.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.blood_donor.data.ConversationDto
import com.example.blood_donor.data.MessageDto
import com.example.blood_donor.data.SendMessageRequest
import com.example.blood_donor.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChatViewModel : ViewModel() {
    private val _conversations = MutableStateFlow<List<ConversationDto>>(emptyList())
    val conversations: StateFlow<List<ConversationDto>> = _conversations.asStateFlow()

    private val _messages = MutableStateFlow<List<MessageDto>>(emptyList())
    val messages: StateFlow<List<MessageDto>> = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun fetchConversations() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = RetrofitClient.apiService.getConversations()
                if (response.isSuccessful) {
                    _conversations.value = response.body()?.conversations ?: emptyList()
                }
            } catch (e: Exception) {
                // handle error
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun fetchChatHistory(userId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = RetrofitClient.apiService.getChatHistory(userId)
                if (response.isSuccessful) {
                    _messages.value = response.body()?.messages ?: emptyList()
                }
            } catch (e: Exception) {
                // handle error
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun sendMessage(receiverId: Int, content: String) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.sendMessage(SendMessageRequest(receiverId, content))
                if (response.isSuccessful) {
                    val newMsg = response.body()?.data
                    if (newMsg != null) {
                        val currentList = _messages.value.toMutableList()
                        currentList.add(newMsg)
                        _messages.value = currentList
                    }
                }
            } catch (e: Exception) {
                // handle error
            }
        }
    }
}
"""

with open(path, "w", encoding="utf-8") as f:
    f.write(content)
