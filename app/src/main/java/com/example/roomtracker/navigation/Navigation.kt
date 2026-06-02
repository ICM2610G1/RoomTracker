package com.example.roomtracker.navigation

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.roomtracker.viewmodel.AuthViewModel
import com.example.roomtracker.viewmodel.FriendsViewModel
import com.example.roomtracker.viewmodel.ChatViewModel
import com.example.roomtracker.viewmodel.LocationViewModel
import com.example.roomtracker.viewmodel.AcademicStatsViewModel
import com.example.roomtracker.viewmodel.CarnetViewModel
import com.example.roomtracker.viewmodel.EventsViewModel
import com.example.roomtracker.viewmodel.OpportunitiesViewModel
import com.example.roomtracker.viewmodel.FoodViewModel
import com.example.roomtracker.viewmodel.ShopViewModel
import com.example.roomtracker.viewmodel.ForoViewModel
import com.example.roomtracker.viewmodel.OperadorViewModel
import com.example.roomtracker.viewmodel.ScheduleViewModel
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
import com.example.roomtracker.ui.screens.LocationRequestsScreen
import com.example.roomtracker.ui.screens.FoodMenuScreen
import com.example.roomtracker.ui.screens.EventsScreen
import com.example.roomtracker.ui.screens.OpportunitiesScreen
import com.example.roomtracker.ui.screens.ScheduleScreen
import com.example.roomtracker.ui.screens.VirtualShopScreen
import com.example.roomtracker.ui.screens.UserCarnetScreen
import com.example.roomtracker.ui.screens.AcademicStatsScreen
import com.example.roomtracker.ui.screens.ForoScreen
import com.example.roomtracker.ui.screens.OperadorHomeScreen
import com.example.roomtracker.ui.screens.QrScannerScreen
import com.example.roomtracker.ui.screens.UniversityStatsScreen
import com.example.roomtracker.viewmodel.PedometerViewModel

enum class AppScreens {
    Login, Register, Verification, RegisterSuccess,
    ForgotPassword, ForgotPasswordSent, HomeMap,
    Settings, Messages, Chat, PrivacyFriends, LocationRequests,
    FoodMenu, Events, Opportunities, MySchedule,
    VirtualShop, UserCarnet, AcademicStats, QrScanner, Foro,
    OperadorHome, OperadorChat, UniversityStats
}

@Composable
fun Navigation() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
    val friendsViewModel: FriendsViewModel = viewModel()
    val chatViewModel: ChatViewModel = viewModel()
    val locationViewModel: LocationViewModel = viewModel()

    val loginState by authViewModel.loginState.collectAsStateWithLifecycle()
    val registerState by authViewModel.registerState.collectAsStateWithLifecycle()
    val verifyState by authViewModel.verifyState.collectAsStateWithLifecycle()
    val forgotState by authViewModel.forgotState.collectAsStateWithLifecycle()
    val resetState by authViewModel.resetState.collectAsStateWithLifecycle()
    val instituciones by authViewModel.instituciones.collectAsStateWithLifecycle()
    val sessionChecked by authViewModel.sessionChecked.collectAsStateWithLifecycle()
    val userTipo by authViewModel.userTipo.collectAsStateWithLifecycle()
    val sesionInvalidada by locationViewModel.sesionInvalidada.collectAsStateWithLifecycle()

    // Si la sesión fue invalidada por otro dispositivo, redirigir al Login
    LaunchedEffect(sesionInvalidada) {
        if (sesionInvalidada) {
            navController.navigate(AppScreens.Login.name) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    android.util.Log.d("RT_NAV", "sessionChecked=$sessionChecked userTipo=$userTipo")
    if (sessionChecked == null) return

    val startDestination = when {
        sessionChecked != true    -> AppScreens.Login.name
        userTipo == "operador"    -> AppScreens.OperadorHome.name
        else                      -> AppScreens.HomeMap.name
    }
    android.util.Log.d("RT_NAV", "startDestination=$startDestination")

    NavHost(navController = navController, startDestination = startDestination) {

        composable(AppScreens.Login.name) {
            LaunchedEffect(loginState, userTipo) {
                if (loginState is AuthViewModel.AuthState.Success && userTipo != null) {
                    authViewModel.resetLoginState()
                    val dest = if (userTipo == "operador") AppScreens.OperadorHome.name
                               else AppScreens.HomeMap.name
                    navController.navigate(dest) {
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
                onOpenMessages = { navController.navigate(AppScreens.Messages.name) },
                onOpenStats    = { navController.navigate(AppScreens.UniversityStats.name) },
                authViewModel  = authViewModel
            )
        }

        composable(AppScreens.UniversityStats.name) {
            val pedometerViewModel: PedometerViewModel = viewModel()
            UniversityStatsScreen(
                onBack    = { navController.popBackStack() },
                viewModel = pedometerViewModel
            )
        }

        composable(AppScreens.Messages.name) {
            MessagesScreen(
                navController = navController,
                onBack = { navController.popBackStack() },
                chatViewModel = chatViewModel
            )
        }

        composable(route = AppScreens.Chat.name + "/{operadorId}/{title}") { backStackEntry ->
            val operadorId = backStackEntry.arguments?.getString("operadorId") ?: ""
            val title = backStackEntry.arguments?.getString("title") ?: ""
            ChatScreen(
                operadorId = operadorId,
                title = title,
                onBack = { navController.popBackStack() },
                chatViewModel = chatViewModel
            )
        }

        composable(AppScreens.PrivacyFriends.name) {
            PrivacyFriendsScreen(
                onBack = { navController.popBackStack() },
                onOpenRequests = { navController.navigate(AppScreens.LocationRequests.name) },
                viewModel = friendsViewModel,
                locationViewModel = locationViewModel
            )
        }

        composable(AppScreens.LocationRequests.name) {
            LocationRequestsScreen(
                onBack = { navController.popBackStack() },
                viewModel = friendsViewModel
            )
        }

        composable(AppScreens.FoodMenu.name) {
            val foodViewModel: FoodViewModel = viewModel()
            FoodMenuScreen(onBack = { navController.popBackStack() }, viewModel = foodViewModel)
        }

        composable(AppScreens.Events.name) {
            val eventsViewModel: EventsViewModel = viewModel()
            EventsScreen(
                onBack = { navController.popBackStack() },
                onStartRoute = { nodeId, name ->
                    // Guardamos el ID del nodo; HomeMapScreen lo resuelve a coordenadas
                    navController.previousBackStackEntry?.savedStateHandle?.set("targetNodeId", nodeId)
                    navController.previousBackStackEntry?.savedStateHandle?.set("targetName", name)
                    navController.popBackStack()
                },
                viewModel = eventsViewModel
            )
        }

        composable(AppScreens.Opportunities.name) {
            val opportunitiesViewModel: OpportunitiesViewModel = viewModel()
            OpportunitiesScreen(
                onBack = { navController.popBackStack() },
                onStartRoute = { nodeId, name ->
                    navController.previousBackStackEntry?.savedStateHandle?.set("targetNodeId", nodeId)
                    navController.previousBackStackEntry?.savedStateHandle?.set("targetName", name)
                    navController.popBackStack()
                },
                viewModel = opportunitiesViewModel
            )
        }

        composable(AppScreens.MySchedule.name) {
            val scheduleViewModel: ScheduleViewModel = viewModel()
            ScheduleScreen(
                onBack = { navController.popBackStack() },
                viewModel = scheduleViewModel
            )
        }

        composable(AppScreens.VirtualShop.name) {
            val shopViewModel: ShopViewModel = viewModel()
            VirtualShopScreen(
                onBack    = { navController.popBackStack() },
                viewModel = shopViewModel,
                onNavigateToStore = {
                    navController.previousBackStackEntry?.savedStateHandle?.set("targetNodeId", "286")
                    navController.previousBackStackEntry?.savedStateHandle?.set("targetName", "Tienda Javeriana")
                    navController.popBackStack()
                }
            )
        }

        composable(AppScreens.UserCarnet.name) {
            val carnetViewModel: CarnetViewModel = viewModel()
            UserCarnetScreen(
                onBack = { navController.popBackStack() },
                onAccessGranted = { navController.navigate(AppScreens.QrScanner.name) },
                viewModel = carnetViewModel
            )
        }

        composable(AppScreens.QrScanner.name) {
            QrScannerScreen(onBack = { navController.popBackStack() })
        }

        composable(AppScreens.AcademicStats.name) {
            val academicStatsViewModel: AcademicStatsViewModel = viewModel()
            AcademicStatsScreen(
                onBack = { navController.popBackStack() },
                viewModel = academicStatsViewModel
            )
        }

        composable(AppScreens.Foro.name) {
            val foroViewModel: ForoViewModel = viewModel()
            ForoScreen(
                onBack    = { navController.popBackStack() },
                viewModel = foroViewModel
            )
        }

        // ─── Rutas de operador ────────────────────────────────────────────────
        composable(AppScreens.OperadorHome.name) {
            val operadorViewModel: OperadorViewModel = viewModel()
            val activity = LocalContext.current as? Activity

            // Bloquear el botón atrás — el operador no puede navegar al mapa
            BackHandler { activity?.finish() }

            OperadorHomeScreen(
                viewModel  = operadorViewModel,
                onOpenChat = { chatId, studentName ->
                    navController.navigate(
                        "${AppScreens.OperadorChat.name}/$chatId/$studentName"
                    )
                },
                onLogout = {
                    authViewModel.logout()
                    activity?.finish() // Cierra la app completamente
                }
            )
        }

        composable("${AppScreens.OperadorChat.name}/{chatId}/{studentName}") { back ->
            val chatId      = back.arguments?.getString("chatId") ?: ""
            val studentName = back.arguments?.getString("studentName") ?: "Estudiante"
            ChatScreen(
                operadorId   = "",          // no usado en modo operador
                title        = studentName,
                onBack       = { navController.popBackStack() },
                chatViewModel = chatViewModel,
                chatIdDirect  = chatId      // nuevo parámetro para modo operador
            )
        }
    }
}
