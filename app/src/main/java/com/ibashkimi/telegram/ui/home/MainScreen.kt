package com.ibashkimi.telegram.ui.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.ibashkimi.telegram.R
import com.ibashkimi.telegram.Screen
import kotlinx.coroutines.launch

@Composable
fun MainScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState
    when (uiState) {
        UiState.Loading -> {
            LoadingScreen(modifier)
        }
        UiState.Loaded -> {
            MainScreenScaffold(navController = navController, modifier = modifier, viewModel = viewModel)
        }
        UiState.Login -> {
            navController.navigate(Screen.Login.route) {
                popUpTo(Screen.Home.route) { inclusive = true }
                launchSingleTop = true
            }
        }
    }
}

@Composable
private fun LoadingScreen(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
fun MainScreenScaffold(
    navController: NavController,
    modifier: Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
    scaffoldState: ScaffoldState = rememberScaffoldState()
) {
    val scope = rememberCoroutineScope()
    Scaffold(
        modifier = modifier,
        scaffoldState = scaffoldState,
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.app_name)) }, navigationIcon = {
                IconButton(
                    onClick = {
                        scope.launch {
                            scaffoldState.drawerState.open()
                        }
                    }) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = null
                    )
                }
            })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate(Screen.CreateChat.route) }) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "New message"
                )
            }
        },
        drawerContent = {
            DrawerContent(client = viewModel.client,
                newGroup = {},
                contacts = {},
                calls = {},
                savedMessages = {},
                settings = {})
        },
        content = {
            Surface(color = MaterialTheme.colors.background) {
                HomeContent(
                    navController,
                    modifier = Modifier.fillMaxWidth(),
                    viewModel = viewModel
                ) {
                    scope.launch {
                        scaffoldState.snackbarHostState.showSnackbar(it)
                    }
                }
            }
        }
    )
}

@Composable
fun HomeContent(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
    showSnackbar: (String) -> Unit
) {
    val chats = viewModel.chats
    var searchParam by remember{ mutableStateOf("")}
    Box(contentAlignment = Alignment.Center) {
        Column {
            TextField(value = searchParam, onValueChange ={
                searchParam = it
                viewModel.search(searchParam)
            })
            ChatsLoaded(
                viewModel.client,
                chats,
                modifier,
                onChatClicked = { viewModel.onSaveChatId(it) },
                showSnackbar
            )
        }
    }

}