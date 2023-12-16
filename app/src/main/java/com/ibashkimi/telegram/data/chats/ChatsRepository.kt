package com.ibashkimi.telegram.data.chats

import androidx.paging.PagingSource
import com.ibashkimi.telegram.data.TelegramClient
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import org.drinkless.td.libcore.telegram.TdApi
import org.drinkless.td.libcore.telegram.TdApi.InputFile
import org.drinkless.td.libcore.telegram.TdApi.InputFileLocal
import javax.inject.Inject

@ExperimentalCoroutinesApi
class ChatsRepository @Inject constructor(private val client: TelegramClient) {

    fun searchChats(searchParam: String, limit: Int): Flow<List<TdApi.Chat>> =
        searchChatIds(searchParam, limit)
            .map { ids -> ids.map { getChat(it) } }
            .flatMapLatest { chatsFlow ->
                combine(chatsFlow) { chats ->
                    chats.toList()
                }
            }
    fun searchChatIds(searchParam: String, limit: Int): Flow<LongArray> =
        callbackFlow {
            client.client.send(TdApi.SearchChatsOnServer(searchParam,  limit)) {
                when (it.constructor) {
                    TdApi.Chats.CONSTRUCTOR -> {
                        trySend((it as TdApi.Chats).chatIds).isSuccess
                    }
                    TdApi.Error.CONSTRUCTOR -> {
                        error("")
                    }
                    else -> {
                        error("")
                    }
                }
                //close()
            }
            awaitClose {}
        }

    fun sendMessage(
    chatId: Long,
    messageThreadId: Long = 0,
    replyToMessageId: Long = 0,
    options: TdApi.MessageSendOptions = TdApi.MessageSendOptions(),
    inputMessageContent: TdApi.InputMessageContent
    ): Deferred<TdApi.Message> = sendMessage(
    TdApi.SendMessage(
    chatId,
    messageThreadId,
    replyToMessageId,
    options,
    null,
    inputMessageContent
    )
    )

    private fun sendMessage(sendMessage: TdApi.SendMessage): Deferred<TdApi.Message> {
        val result = CompletableDeferred<TdApi.Message>()
        client.client.send(sendMessage) {
            when (it.constructor) {
                TdApi.Message.CONSTRUCTOR -> {
                    result.complete(it as TdApi.Message)
                }
                else -> {
                    result.completeExceptionally(error("Something went wrong"))
                }
            }
        }
        return result
    }
    private fun getChatIds(offsetOrder: Long = Long.MAX_VALUE, limit: Int): Flow<LongArray> =
        callbackFlow {
            client.client.send(TdApi.GetChats(TdApi.ChatListMain(),  limit)) {
                when (it.constructor) {
                    TdApi.Chats.CONSTRUCTOR -> {
                        trySend((it as TdApi.Chats).chatIds).isSuccess
                    }
                    TdApi.Error.CONSTRUCTOR -> {
                        error("")
                    }
                    else -> {
                        error("")
                    }
                }
                //close()
            }
            awaitClose { }
        }

    fun getChats(offsetOrder: Long = Long.MAX_VALUE, limit: Int): Flow<List<TdApi.Chat>> =
        getChatIds(offsetOrder, limit)
            .map { ids -> ids.map { getChat(it) } }
            .flatMapLatest { chatsFlow ->
                combine(chatsFlow) { chats ->
                    chats.toList()
                }
            }

    fun getChat(chatId: Long): Flow<TdApi.Chat> = callbackFlow {
        client.client.send(TdApi.GetChat(chatId)) {
            when (it.constructor) {
                TdApi.Chat.CONSTRUCTOR -> {
                    trySend(it as TdApi.Chat).isSuccess
                }
                TdApi.Error.CONSTRUCTOR -> {
                    error("Something went wrong")
                }
                else -> {
                    error("Something went wrong")
                }
            }
            //close()
        }
        awaitClose { }
    }

    fun chatImage(chat: TdApi.Chat): Flow<String?> =
        chat.photo?.small?.takeIf {
            it.local?.isDownloadingCompleted == false
        }?.id?.let { fileId ->
            client.downloadFile(fileId).map { chat.photo?.small?.local?.path }
        } ?: flowOf(chat.photo?.small?.local?.path)
}