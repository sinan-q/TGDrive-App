package com.ibashkimi.telegram.ui.util

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import coil.compose.AsyncImage
import com.ibashkimi.telegram.data.TelegramClient
import kotlinx.coroutines.Dispatchers
import org.drinkless.td.libcore.telegram.TdApi
import java.io.File

@SuppressLint("UnrememberedMutableState")
@Composable
fun TelegramImage(
    client: TelegramClient,
    file: TdApi.File?,
    modifier: Modifier = Modifier
) {
    val photo = file?.let {
        client.downloadableFile(file).collectAsState(file.local.path, Dispatchers.IO)
    }
    photo?.value?.let {
        AsyncImage(
            model = File(it),
            contentDescription = null,
            modifier = modifier
        )
    } ?: Box(modifier.background(Color.LightGray))
}
