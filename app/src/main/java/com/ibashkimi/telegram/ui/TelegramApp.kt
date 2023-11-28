package com.ibashkimi.telegram.ui

import android.annotation.SuppressLint
import android.app.Activity
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.toArgb
import androidx.hilt.navigation.compose.hiltNavGraphViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.ibashkimi.telegram.Screen
import com.ibashkimi.telegram.ui.chat.ChatScreen
import com.ibashkimi.telegram.ui.chat.ChatScreenViewModel
import com.ibashkimi.telegram.ui.createchat.CreateChatScreen
import com.ibashkimi.telegram.ui.home.HomeViewModel
import com.ibashkimi.telegram.ui.home.MainScreen
import com.ibashkimi.telegram.ui.login.LoginScreen
import com.ibashkimi.telegram.ui.login.LoginViewModel
import com.ibashkimi.telegram.ui.theme.TelegramTheme

@Composable
fun TelegramApp(activity: Activity) {
    TelegramTheme {
        activity.window.statusBarColor = MaterialTheme.colors.primaryVariant.toArgb()
        val navController = rememberNavController()
        MainNavHost(navController)
    }
}

@SuppressLint("UnrememberedGetBackStackEntry")
@Composable
private fun MainNavHost(navController: NavHostController, homeViewModel: HomeViewModel = hiltViewModel(), loginViewModel: LoginViewModel = hiltViewModel()) {
    NavHost(navController, startDestination = Screen.Home.route) {
        composable(Screen.Home.route) {
            MainScreen(navController = navController, viewModel = homeViewModel)
        }
        composable(Screen.Chat.route) {
            val chatId = Screen.Chat.getChatId(it)
            val viewModel: ChatScreenViewModel =
                hiltViewModel(navController.getBackStackEntry(Screen.Chat.route))
            viewModel.setChatId(chatId)
            ChatScreen(
                chatId = chatId,
                navController = navController,
                viewModel = viewModel
            )
        }
        composable(Screen.CreateChat.route) {
            CreateChatScreen(
                navigateUp = navController::navigateUp,
                viewModel = hiltViewModel(it)
            )
        }
        composable(Screen.Login.route) {
            LoginScreen(loginViewModel) {
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Login.route) { inclusive = true }
                }
            }
        }
    }
}
