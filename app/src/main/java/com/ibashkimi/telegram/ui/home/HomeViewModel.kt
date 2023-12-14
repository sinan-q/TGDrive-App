package com.ibashkimi.telegram.ui.home

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ibashkimi.telegram.data.Authentication
import com.ibashkimi.telegram.data.TelegramClient
import com.ibashkimi.telegram.data.UserRepository
import com.ibashkimi.telegram.data.chats.ChatsPagingSource
import com.ibashkimi.telegram.data.chats.ChatsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.drinkless.td.libcore.telegram.TdApi
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    val client: TelegramClient,
    private val chatsPagingSource: ChatsPagingSource,
    private val chatsRepository: ChatsRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    val uiState = mutableStateOf<UiState>(UiState.Loading)
    init {

        client.authState.onEach {
            when (it) {
                Authentication.UNAUTHENTICATED -> {
                    client.startAuthentication()
                }
                Authentication.WAIT_FOR_NUMBER, Authentication.WAIT_FOR_CODE, Authentication.WAIT_FOR_PASSWORD -> uiState.value =
                    UiState.Login
                Authentication.AUTHENTICATED -> uiState.value = UiState.Loaded
                Authentication.UNKNOWN -> {
                }
            }
        }.launchIn(viewModelScope)
    }
    @OptIn(ExperimentalCoroutinesApi::class)
    fun search(seachParam: String) {
        viewModelScope.launch {
         chatsRepository.searchChats(seachParam,10).collectLatest {
             chats = it
         }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun onSaveChatId(chatId: Long) {
        userRepository.chatId = chatId
    }

    var chats = emptyList<TdApi.Chat>()

}

sealed class UiState {
    object Loading : UiState()
    object Login : UiState()
    object Loaded : UiState()
}