package com.ibashkimi.telegram.ui.home

import android.app.Service
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.ibashkimi.telegram.data.Authentication
import com.ibashkimi.telegram.data.TelegramClient
import com.ibashkimi.telegram.data.UserRepository
import com.ibashkimi.telegram.data.chats.ChatsPagingSource
import com.ibashkimi.telegram.data.chats.ChatsRepository
import com.ibashkimi.telegram.data.messages.MessagesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.drinkless.td.libcore.telegram.TdApi
import org.drinkless.td.libcore.telegram.TdApi.InputFileLocal
import org.drinkless.td.libcore.telegram.TdApi.InputMessageDocument
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.security.SecureRandom
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import kotlin.properties.Delegates

@HiltViewModel
class HomeViewModel @Inject constructor(
    val client: TelegramClient,
    private val chatsPagingSource: ChatsPagingSource,
    private val chatsRepository: ChatsRepository,
    private val userRepository: UserRepository,
    private val messagesRepository: MessagesRepository
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

        userRepository.chatId?.let { setChatId(it) }
    }
    @OptIn(ExperimentalCoroutinesApi::class)
    fun search(seachParam: String) {
        viewModelScope.launch {
         chatsRepository.searchChats(seachParam,10).collectLatest {
         //    chats = it
         }
        }
    }


    @OptIn(ExperimentalCoroutinesApi::class)
    fun onSaveChatId(chatId: Long) {
        userRepository.chatId = chatId
    }

    fun uploadFiles(uris: List<Uri>, context: Context) {
        if (uris.isNotEmpty()) {
            val downloadJob = CoroutineScope(Dispatchers.IO).launch {
                for (uri in uris) {
                    try {
                        val filename = context.contentResolver.query(uri, null, null, null, null)?.use {
                            val nameColumnIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                            it.moveToFirst()
                            it.getString(nameColumnIndex)
                        } ?: "Unknown"
                        val tempFile = File(context.cacheDir, "filename").also {
                            if (it.exists()) it.delete()
                        }
                        encryptFile(context.contentResolver.openInputStream(uri)!!, tempFile.outputStream(), "5b0904cfada01b8182bcc029b928244d")
                        val size = tempFile.length()

                        val d = messagesRepository.sendMessage(chatId = chatId, inputMessageContent = InputMessageDocument(InputFileLocal(tempFile.path),null,false,TdApi.FormattedText(
                            LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE),
                            emptyArray()
                        )) )
                        Log.d("TAG", "uploadFiles: ${ d.await().chatId }")


                    } catch (_: Exception) {
                    }
                }
            }
        }
    }
    private var chatId = -1L
    lateinit var messagesPaged: Flow<PagingData<TdApi.Message>>
    lateinit var chat: Flow<TdApi.Chat>
    private fun setChatId(chatId: Long) {
        this.chatId = chatId
        this.chat = chatsRepository.getChat(chatId)
        this.messagesPaged = Pager(PagingConfig(pageSize = 30)) {
            messagesRepository.getMessagesPaged(chatId)
        }.flow.cachedIn(viewModelScope)
    }
}

sealed class UiState {
    object Loading : UiState()
    object Login : UiState()
    object Loaded : UiState()
}
fun encryptFile(input: InputStream, output: FileOutputStream, password: String) {
    val key = SecretKeySpec(password.toByteArray(), "AES")
    val iv = ByteArray(16)
    val secureRandom = SecureRandom()
    secureRandom.nextBytes(iv)

    val ivSpec = IvParameterSpec(iv)
    val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
    cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec)

    // Write the IV to the beginning of the output file
    output.write(iv)

    val buffer = ByteArray(4096)
    var read = input.read(buffer)
    while (read != -1) {
        val encrypted = cipher.update(buffer,0,read)

        output.write(encrypted)
        read = input.read(buffer)
    }
    val finalEncrypted = cipher.doFinal()
    output.write(finalEncrypted)

    output.flush()
    output.close()
}