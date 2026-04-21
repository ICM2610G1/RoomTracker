package com.example.roomtracker.navigation

import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.roomtracker.viewmodel.AuthViewModel
import com.example.roomtracker.ui.screens.ChatScreen
import com.example.roomtracker.ui.screens.ForgotPasswordSentScreen
import com.example.roomtracker.ui.screens.LoginScreen
import com.example.roomtracker.ui.screens.RegisterScreen
import com.example.roomtracker.ui.screens.VerificationScreen
import com.example.roomtracker.ui.screens.RegisterSuccessScreen
import com.example.roomtracker.ui.screens.ForgotPasswordScreen
import com.example.roomtracker.ui.screens.HomeMapScreen
import com.example.roomtracker.ui.screens.SettingsScreen
import com.example.roomtracker.ui.screens.MessagesScreen
import com.example.roomtracker.ui.screens.PrivacyFriendsScreen
import com.example.roomtracker.ui.screens.FoodMenuScreen
import com.example.roomtracker.ui.screens.EventsScreen
import com.example.roomtracker.ui.screens.OpportunitiesScreen
import com.example.roomtracker.ui.screens.ScheduleScreen
import com.example.roomtracker.ui.screens.VirtualShopScreen
import com.example.roomtracker.ui.screens.UserCarnetScreen
import com.example.roomtracker.ui.screens.AcademicStatsScreen

enum class AppScreens {
    Login, Register, Verification, RegisterSuccess,
    ForgotPassword, ForgotPasswordSent, HomeMap,
    Settings, Messages, Chat, PrivacyFriends,
    FoodMenu, Events, Opportunities, MySchedule,
    VirtualShop, UserCarnet, AcademicStats
}

@Composable
fun Navigation() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()

    val loginState by authViewModel.loginState.collectAsStateWithLifecycle()
    val registerState by authViewModel.registerState.collectAsStateWithLifecycle()
    val verifyState by authViewModel.verifyState.collectAsStateWithLifecycle()
    val forgotState by authViewModel.forgotState.collectAsStateWithLifecycle()
    val resetState by authViewModel.resetState.collectAsStateWithLifecycle()
    val instituciones by authViewModel.instituciones.collectAsStateWithLifecycle()
    val sessionChecked by authViewModel.sessionChecked.collectAsStateWithLifecycle()

    // Esperar a que se verifique la sesión antes de mostrar nada
    android.util.Log.d("RT_NAV", "sessionChecked=$sessionChecked")
    if (sessionChecked == null) return

    val startDestination = if (sessionChecked == true) AppScreens.HomeMap.name else AppScreens.Login.name
    android.util.Log.d("RT_NAV", "startDestination=$startDestination")

    NavHost(navController = navController, startDestination = startDestination) {

        composable(AppScreens.Login.name) {
            LaunchedEffect(loginState) {
                if (loginState is AuthViewModel.AuthState.Success) {
                    authViewModel.resetLoginState()
                    navController.navigate(AppScreens.HomeMap.name) {
                        popUpTo(AppScreens.Login.name) { inclusive = true }
                    }
                }
            }
            LoginScreen(
                instituciones = instituciones,
                onLoginClick = { email, password, institucion ->
                    authViewModel.login(email, password, institucion)
                },
                onRegisterClick = {
                    authViewModel.resetLoginState()
                    navController.navigate(AppScreens.Register.name)
                },
                onForgotPasswordClick = {
                    navController.navigate(AppScreens.ForgotPassword.name)
                },
                isLoading = loginState is AuthViewModel.AuthState.Loading,
                errorMessage = (loginState as? AuthViewModel.AuthState.Error)?.message
            )
        }

        composable(AppScreens.Register.name) {
            LaunchedEffect(registerState) {
                if (registerState is AuthViewModel.AuthState.Success) {
                    authViewModel.resetRegisterState()
                    navController.navigate(AppScreens.Verification.name)
                }
            }
            RegisterScreen(
                onVerifyClick = { nombre, apellido, email, password ->
                    authViewModel.register(nombre, apellido, email, password)
                },
                onBackToLogin = {
                    authViewModel.resetRegisterState()
                    navController.popBackStack()
                },
                isLoading = registerState is AuthViewModel.AuthState.Loading,
                errorMessage = (registerState as? AuthViewModel.AuthState.Error)?.message
            )
        }

        composable(AppScreens.Verification.name) {
            LaunchedEffect(verifyState) {
                if (verifyState is AuthViewModel.AuthState.Success) {
                    authViewModel.resetVerifyState()
                    navController.navigate(AppScreens.RegisterSuccess.name)
                }
            }
            VerificationScreen(
                onVerifyClick = { code -> authViewModel.verifyOtp(code) },
                onBack = { navController.popBackStack() },
                isLoading = verifyState is AuthViewModel.AuthState.Loading,
                errorMessage = (verifyState as? AuthViewModel.AuthState.Error)?.message
            )
        }

        composable(AppScreens.RegisterSuccess.name) {
            RegisterSuccessScreen(
                onGoToLogin = {
                    navController.navigate(AppScreens.Login.name) { popUpTo(0) }
                }
            )
        }

        composable(AppScreens.ForgotPassword.name) {
            LaunchedEffect(forgotState) {
                if (forgotState is AuthViewModel.AuthState.Success) {
                    authViewModel.resetForgotState()
                    navController.navigate(AppScreens.ForgotPasswordSent.name)
                }
            }
            ForgotPasswordScreen(
                onSendEmail = { email -> authViewModel.forgotPassword(email) },
                onBack = { navController.popBackStack() },
                isLoading = forgotState is AuthViewModel.AuthState.Loading,
                errorMessage = (forgotState as? AuthViewModel.AuthState.Error)?.message
            )
        }

        composable(AppScreens.ForgotPasswordSent.name) {
            LaunchedEffect(resetState) {
                if (resetState is AuthViewModel.AuthState.Success) {
                    authViewModel.resetResetState()
                    navController.navigate(AppScreens.Login.name) {
                        popUpTo(AppScreens.Login.name) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            }
            ForgotPasswordSentScreen(
                onResetPassword = { otp, newPassword -> authViewModel.resetPassword(otp, newPassword) },
                onBackToLogin = {
                    authViewModel.resetForgotState()
                    authViewModel.resetResetState()
                    navController.popBackStack(AppScreens.Login.name, inclusive = false)
                },
                isLoading = resetState is AuthViewModel.AuthState.Loading,
                errorMessage = (resetState as? AuthViewModel.AuthState.Error)?.message
            )
        }

        composable(AppScreens.HomeMap.name) {
            HomeMapScreen(navController)
        }

        composable(AppScreens.Settings.name) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(AppScreens.Login.name) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onOpenMessages = { navController.navigate(AppScreens.Messages.name) }
            )
        }

        composable(AppScreens.Messages.name) {
            MessagesScreen(navController = navController, onBack = { navController.popBackStack() })
        }

        composable(route = AppScreens.Chat.name + "/{title}") { backStackEntry ->
            val title = backStackEntry.arguments?.getString("title") ?: ""
            ChatScreen(title = title, onBack = { navController.popBackStack() })
        }

        composable(AppScreens.PrivacyFriends.name) {
            PrivacyFriendsScreen(
                onBack = { navController.popBackStack() },
                onOpenRequests = {}
            )
        }

        composable(AppScreens.FoodMenu.name) {
            FoodMenuScreen(onBack = { navController.popBackStack() })
        }

        composable(AppScreens.Events.name) {
            EventsScreen(
                onBack = { navController.popBackStack() },
                onStartRoute = { location, name ->
                    navController.previousBackStackEntry?.savedStateHandle?.set("targetLocation", location)
                    navController.previousBackStackEntry?.savedStateHandle?.set("targetName", name)
                    navController.popBackStack()
                }
            )
        }

        composable(AppScreens.Opportunities.name) {
            OpportunitiesScreen(onBack = { navController.popBackStack() })
        }

        composable(AppScreens.MySchedule.name) {
            ScheduleScreen(onBack = { navController.popBackStack() })
        }

        composable(AppScreens.VirtualShop.name) {
            VirtualShopScreen(onBack = { navController.popBackStack() })
        }

        composable(AppScreens.UserCarnet.name) {
            UserCarnetScreen(
                onBack = { navController.popBackStack() },
                onAccessGranted = {
                    // Acción opcional al conceder acceso
                }
            )
        }

        composable(AppScreens.AcademicStats.name) {
            AcademicStatsScreen(onBack = { navController.popBackStack() })
        }
    }
}
