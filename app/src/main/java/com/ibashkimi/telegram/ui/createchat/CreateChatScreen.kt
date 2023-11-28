package com.ibashkimi.telegram.ui.createchat

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material.icons.outlined.Speaker
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ibashkimi.telegram.data.TelegramClient
import com.ibashkimi.telegram.ui.util.TelegramImage
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import org.drinkless.td.libcore.telegram.TdApi

@Composable
fun CreateChatScreen(
    navigateUp: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CreateChatViewModel = viewModel()
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("New message") },
                navigationIcon = {
                    IconButton(onClick = { navigateUp() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = null
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /*TODO*/ }) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search contact"
                        )
                    }
                    IconButton(onClick = { /*TODO*/ }) {
                        Icon(
                            imageVector = Icons.Default.Sort,
                            contentDescription = "Sort"
                        )
                    }
                })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {}) {
                Icon(
                    imageVector = Icons.Outlined.PersonAdd,
                    contentDescription = "New contact"
                )
            }
        },
        content = {
            CreateChatContent(viewModel.client, viewModel.users)
        }
    )
}

@OptIn(ExperimentalMaterialApi::class, ExperimentalCoroutinesApi::class)
@Composable
private fun CreateChatContent(
    client: TelegramClient,
    users: Flow<List<TdApi.User>>,
    modifier: Modifier = Modifier
) {
    val usersState = users.collectAsState(initial = null)
    LazyColumn(modifier = modifier) {
        item {
            ListItem(icon = {
                Icon(imageVector = Icons.Outlined.People, contentDescription = null)
            }) {
                Text("New Group")
            }
        }
        item {
            ListItem(icon = {
                Icon(imageVector = Icons.Outlined.Lock, contentDescription = null)
            }) {
                Text("New Secret Chat")
            }
        }
        item {
            ListItem(icon = {
                Icon(imageVector = Icons.Outlined.Speaker, contentDescription = null)
            }) {
                Text("New Channel")
            }
        }
        usersState.value?.let { userList ->
            item {
                Divider()
            }
            items(userList) {
                ContactItem(client, it)
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ContactItem(client: TelegramClient, user: TdApi.User, modifier: Modifier = Modifier) {
    ListItem(modifier = modifier,
        icon = {
            TelegramImage(
                client = client,
                file = user.profilePhoto?.small,
                modifier = Modifier
                    .clip(shape = CircleShape)
                    .size(42.dp)
            )
        },
        secondaryText = {
            Text(user.username ?: "")
        }
    ) {
        Text(user.run { "$firstName $lastName" })
    }
}
