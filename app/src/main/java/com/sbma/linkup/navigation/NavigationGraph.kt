package com.sbma.linkup.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.sbma.linkup.application.connectivity.InternetConnectionState
import com.sbma.linkup.application.data.AppViewModelProvider
import com.sbma.linkup.card.CardViewModel
import com.sbma.linkup.connection.ConnectionViewModel
import com.sbma.linkup.presentation.screens.CameraScreen
import com.sbma.linkup.presentation.screens.ConnectionUserProfileScreenProvider
import com.sbma.linkup.presentation.screens.EditProfileScreenProvider
import com.sbma.linkup.presentation.screens.MainShareScreen
import com.sbma.linkup.presentation.screens.ScanResultScreen
import com.sbma.linkup.presentation.screens.SettingScreen
import com.sbma.linkup.presentation.screens.UserConnectionsScreen
import com.sbma.linkup.presentation.screens.UserProfileScreen
import com.sbma.linkup.presentation.screens.UserShareScreen
import com.sbma.linkup.presentation.screens.bluetooth.ShareViaBluetoothScreenProvider
import com.sbma.linkup.presentation.screens.nfc.NfcReceiveScreen
import com.sbma.linkup.presentation.screens.nfc.NfcScanScreen
import com.sbma.linkup.presentation.screenstates.UserConnectionsScreenState
import com.sbma.linkup.user.User
import com.sbma.linkup.user.UserViewModel
import com.sbma.linkup.util.MyQrCode
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


@Composable
fun NavigationGraph(
    navController: NavHostController,
    user: User,
    internetConnectionStateFlow: StateFlow<InternetConnectionState>,
    modifier: Modifier = Modifier
) {
    val userCardViewModel: CardViewModel = viewModel(factory = AppViewModelProvider.Factory)
    val userConnectionViewModel: ConnectionViewModel =
        viewModel(factory = AppViewModelProvider.Factory)
    val userCards = userCardViewModel.allItemsStream(user.id).collectAsState(initial = listOf())
    val internetState = internetConnectionStateFlow.collectAsState()

    val userViewModel: UserViewModel = viewModel(factory = AppViewModelProvider.Factory)
    val composableScope = rememberCoroutineScope()
    val userItems = userConnectionViewModel
        .allItemsStream(user.id)
        .collectAsState(initial = mapOf())

    val state = UserConnectionsScreenState(
        connections = userItems.value
    )

    NavHost(
        navController,
        modifier = modifier,
        startDestination = "profile"
    ) {
        /**
         * tab of the bottom navigation bar
         */
        composable("share") {
            UserShareScreen(
                userCards.value,
            ) { cardsToShare ->
                composableScope.launch {
                    if (!internetState.value.isConnected()) {
                        // TODO: Show a Toast or something and let the user know this action requires internet.
                        return@launch
                    }
                    val cardIds = cardsToShare.map { card -> card.id.toString() }
                    userViewModel.shareCards(cardIds) { shareId ->
                        navController.navigate("share/${shareId}")
                    }
                }
            }
        }
        /**
         * Simple page with three buttons and three callbacks which we can navigate to the wanted route.
         */
        composable(
            "share/{shareId}",
            arguments = listOf(navArgument("shareId") { type = NavType.StringType }),

            ) { backStackEntry ->
            val shareId = backStackEntry.arguments?.getString("shareId")
            MainShareScreen(
                onBluetoothClick = {
                    navController.navigate("share/${shareId}/bluetooth")
                },
                onNfcClick = {
                    navController.navigate("share/${shareId}/nfc")
                },
                onQrCodeClick = {
                    navController.navigate("share/${shareId}/qr")
                },
                isReceiving = false,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
        /**
         * Bluetooth method of sharing user profile.
         * at this point json string should be already saved to datastore and available.
         */
        composable(
            "share/{shareId}/bluetooth",
            arguments = listOf(navArgument("shareId") { type = NavType.StringType })
        ) { backStackEntry ->
            val shareId = backStackEntry.arguments?.getString("shareId")
            shareId?.let {
                ShareViaBluetoothScreenProvider(it)
            }
        }

        composable(
            "share/{shareId}/nfc",
            arguments = listOf(navArgument("shareId") { type = NavType.StringType })
        ) { backStackEntry ->
            val shareId = backStackEntry.arguments?.getString("shareId")
            shareId?.let {
                NfcScanScreen(
                    shareId = it,
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }
        }
        composable(
            "share/{shareId}/qr",
            arguments = listOf(navArgument("shareId") { type = NavType.StringType })
        ) { backStackEntry ->
            val shareId = backStackEntry.arguments?.getString("shareId")
            shareId?.let {
                MyQrCode(
                    shareId = it,
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }

        }
        /**
         * tab of the bottom navigation bar
         */
        composable("connections") {
            UserConnectionsScreen(state) { connection ->
                navController.navigate("connections/${connection.id}")
            }
        }
        /**
         * When user selected one of their connections it should redirect to the profile screen of the connection user
         * And show the profile with cards that user has access to.
         */
        composable(
            "connections/{connectionId}",
            arguments = listOf(navArgument("connectionId") { type = NavType.StringType }),

            ) { backStackEntry ->
            ConnectionUserProfileScreenProvider(
                user,
                backStackEntry.arguments?.getString("connectionId")
            )
        }
        /**
         * tab of the bottom navigation bar
         */
        composable("receive") {
            MainShareScreen(
                onBluetoothClick = {
                    navController.navigate("receive/bluetooth")
                },
                onNfcClick = {
                    navController.navigate("receive/nfc")
                },
                onQrCodeClick = {
                    navController.navigate("receive/qr")
                },
                isReceiving = true,
                onBackClick = {
                    navController.popBackStack()
                }

            )
        }
        /**
         * Bluetooth method of sharing user profile.
         * at this point json string should be already saved to datastore and available.
         */
        composable("receive/bluetooth") {
        }
        composable("receive/nfc") {
            NfcReceiveScreen(
                onBackClick = {
                    navController.popBackStack()
                })
        }
        composable("receive/qr") {
            CameraScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onSuccessScan = {
                    navController.navigate("scanSuccess")
                }
            )
        }
        /**
         * tab of the bottom navigation bar
         */
        composable(BottomNavItem.Setting.screen_route) {
            SettingScreen()
        }
        /**
         * tab of the bottom navigation bar
         * Navigates to {UserShareScreenProvider}  when user clicks on Share button.
         */
        composable("profile") {
            UserProfileScreen(
                user,
                userCards.value,
                canEdit = true,
                onEditClick = { navController.navigate("profile/edit") }
            )
        }
        /**
         *  Edit profile screen
         *  After user presses save button it will navigate back to profile route.
         */
        composable("profile/edit") {
            EditProfileScreenProvider(
                user,
                userCards.value,
                onBackClick = {
                    navController.popBackStack()
                },
                onSave = {
                    navController.navigate("profile")
                })
        }
        composable(route = "scanSuccess") {
            ScanResultScreen(

            )
        }
    }
}
