package com.ibashkimi.telegram.ui.home

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.ibashkimi.telegram.R
import com.ibashkimi.telegram.Screen
import kotlinx.coroutines.launch
import org.drinkless.td.libcore.telegram.TdApi
import org.drinkless.td.libcore.telegram.TdApi.MessageDocument
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

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
    val context = LocalContext.current
    val startActivityIntent = rememberLauncherForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri>? -> if (uris!=null && uris.isNotEmpty()) {
        viewModel.uploadFiles(uris, context)
    } }
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
            FloatingActionButton(onClick = { startActivityIntent.launch("*/*")
            }) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "New message"
                )
            }
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

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun HomeContent(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
    showSnackbar: (String) -> Unit
) {
    val chats = viewModel.messagesPaged
    var searchParam by remember{ mutableStateOf("")}
    Box(contentAlignment = Alignment.Center) {
        Column {
            TextField(value = searchParam, onValueChange ={
                searchParam = it
                viewModel.search(searchParam)
            })
            var expandedIndex by remember { mutableIntStateOf(-1) }

            val messages = viewModel.messagesPaged.collectAsLazyPagingItems()
//            val uploadProgress = fileViewModel.progress.value
//            val animatedProgress by animateFloatAsState(targetValue = uploadProgress,animationSpec = TweenSpec(durationMillis = 2000))
//            val breadCrumb by viewModel.pathList.collectAsState()
            Box(modifier = Modifier.fillMaxSize()) {
                Column {
                    Divider()
//                    Text(text = breadCrumb.joinToString(">") { " ${it.pathName} " })
                    Divider()
//                    Box(
//                        modifier = Modifier
//                            .height(15.dp)
//                            .fillMaxWidth(animatedProgress)
//                            .background(color = Color.Blue)
//                    )

                    LazyColumn {
                        items(
                            count = messages.itemCount,
                            key = messages.itemKey(),
                            contentType = messages.itemContentType()
                        ) { i ->
                            val message = messages[i]
                            val doc = when (message!!.content.constructor) {
                                TdApi.MessageDocument.CONSTRUCTOR -> {
                                    message.content as MessageDocument
                                }
                                else -> {
                                    return@items
                                }
                            }
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 15.dp, end = 15.dp, top = 5.dp),
                                onClick = {
                                    TODO()
                                }) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    val imageModifier = Modifier
                                        .padding(10.dp)
                                        .size(50.dp)
                                    Image(
                                        modifier = imageModifier,
                                        painter = painterResource(R.drawable.ic_document),
                                        contentDescription = null
                                    )
                                    Column(modifier = Modifier.fillMaxWidth(0.84f)) {
                                        Text(
                                            text = doc.document.fileName,
                                            color = if (!doc.document.document.remote.isUploadingCompleted) Color.Red else Color.Unspecified,
                                            fontSize = 18.sp, fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "${convertDate(message.date)}",
                                            fontSize = 10.sp
                                        )

                                    }
                                }
                            }
                        }


                    }
                }

            }
        }
    }

}

fun convertDate(inputDate: Int): String {
    val instant = Instant.ofEpochSecond(inputDate.toLong())
    val zonedDateTime = instant.atZone(ZoneId.systemDefault())
    return zonedDateTime.format(DateTimeFormatter.ofPattern("dd-MM-yy HH:mm"))
}