package com.sbma.linkup.navigation

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.sbma.linkup.application.data.AppViewModelProvider
import com.sbma.linkup.presentation.screens.NewProfileScreen
import com.sbma.linkup.user.User
import com.sbma.linkup.user.UserViewModel
import kotlinx.coroutines.launch
import java.util.UUID

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun NavigationView(
    userViewModel: UserViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val composableScope = rememberCoroutineScope()
    val context = LocalContext.current

    val navController = rememberNavController()
    val loggedInUser = userViewModel.getLoggedInUserProfile.collectAsState(initial = null)

    loggedInUser.value?.let { user ->
        Scaffold(
            bottomBar = { BottomNavigationBar(navController) }
        ) {
            NavigationGraph(navController, user)
        }
    } ?: Scaffold {
        NewProfileScreen {
            composableScope.launch {
                val userId = UUID.randomUUID()
                userViewModel.insertItem(User(userId, it.username, it.description))
                userViewModel.setLoggedInUserId(userId)
                Toast.makeText(context, "Profile Created!", Toast.LENGTH_SHORT).show()
            }
        }
    }

}
