package com.ibashkimi.telegram.data

import android.content.SharedPreferences
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import org.drinkless.td.libcore.telegram.TdApi
import javax.inject.Inject

@ExperimentalCoroutinesApi
class UserRepository @Inject constructor(
    private val client: TelegramClient,
    private val preferences: SharedPreferences,

    ) {

    fun getUser(userId: Long): Flow<TdApi.User> = callbackFlow {
        client.client.send(TdApi.GetUser(userId)) {
            trySend(it as TdApi.User).isSuccess
        }
        awaitClose { }
    }

    var chatId: Long?
        get() = preferences.getLong("chatId",0L)
        set(token) {
            preferences.edit().putLong("chatId", token?:0L).apply()
        }
}