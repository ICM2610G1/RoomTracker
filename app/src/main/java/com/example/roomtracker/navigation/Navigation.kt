package com.example.roomtracker.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.roomtracker.screens.ChatScreen
import com.example.roomtracker.screens.ForgotPasswordSentScreen
import com.example.roomtracker.screens.LoginScreen
import com.example.roomtracker.screens.RegisterScreen
import com.example.roomtracker.screens.VerificationScreen
import com.example.roomtracker.screens.RegisterSuccessScreen
import com.example.roomtracker.screens.ForgotPasswordScreen
import com.example.roomtracker.screens.HomeMapScreen
import com.example.roomtracker.screens.SettingsScreen
import com.example.roomtracker.screens.MessagesScreen
import com.example.roomtracker.screens.PrivacyFriendsScreen

enum class AppScreens {
    Login,
    Register,
    Verification,
    RegisterSuccess,
    ForgotPassword,
    ForgotPasswordSent,
    HomeMap,
    Settings,
    Messages,
    Chat,
    PrivacyFriends
}
@Composable
fun Navigation() {

    val navController = rememberNavController()


    NavHost(
        navController = navController,
        startDestination = AppScreens.HomeMap.name
    ) {

        composable(AppScreens.Login.name) {
            LoginScreen(
                onLoginClick = { },
                onRegisterClick = {
                    navController.navigate(AppScreens.Register.name)
                },
                onForgotPasswordClick = {
                    navController.navigate(AppScreens.ForgotPassword.name)
                }
            )
        }

        composable(AppScreens.Register.name) {
            RegisterScreen(
                onVerifyClick = {
                    navController.navigate(AppScreens.Verification.name)
                },
                onBackToLogin = {
                    navController.popBackStack()
                }
            )
        }

        composable(AppScreens.Verification.name) {
            VerificationScreen(
                onCodeVerified = {
                    navController.navigate(AppScreens.RegisterSuccess.name)
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(AppScreens.RegisterSuccess.name) {
            RegisterSuccessScreen(
                onGoToLogin = {
                    navController.navigate(AppScreens.Login.name) {
                        popUpTo(0)
                    }
                }
            )
        }

        composable(AppScreens.ForgotPassword.name) {
            ForgotPasswordScreen(
                onSendEmail = {
                    navController.navigate(AppScreens.ForgotPasswordSent.name)
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(AppScreens.ForgotPasswordSent.name) {
            ForgotPasswordSentScreen(
                onBackToLogin = {
                    navController.navigate(AppScreens.Login.name) {
                        popUpTo(0)
                    }
                }
            )
        }

        composable(AppScreens.HomeMap.name) {
            HomeMapScreen(navController)
        }

        composable(AppScreens.Settings.name) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onLogout = {
                    navController.navigate(AppScreens.Login.name) {
                        popUpTo(0)
                    }
                },
                onOpenMessages = {
                    navController.navigate(AppScreens.Messages.name)
                }
            )
        }
        composable(AppScreens.Messages.name) {
            MessagesScreen(
                navController = navController,
                onBack = { navController.popBackStack() }
            )

        }
        composable(
            route = AppScreens.Chat.name + "/{title}"
        ) { backStackEntry ->

            val title = backStackEntry.arguments?.getString("title") ?: ""

            ChatScreen(
                title = title,
                onBack = { navController.popBackStack() }
            )
        }
        composable(AppScreens.PrivacyFriends.name) {
            PrivacyFriendsScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}