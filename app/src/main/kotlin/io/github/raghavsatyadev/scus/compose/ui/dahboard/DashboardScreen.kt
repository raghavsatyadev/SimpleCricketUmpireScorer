package io.github.raghavsatyadev.scus.compose.ui.dahboard

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun DashboardScreen(
    viewModel: DashboardScreenViewModel = hiltViewModel(),
    onNavigateToLogin: () -> Unit,
) {
    if (!viewModel.isLoggedIn()) {
        onNavigateToLogin()
    } else {
        Scaffold(modifier = Modifier) { innerPadding ->
            Column(modifier = Modifier.padding(innerPadding)) { Text(text = "Dashboard") }
        }
    }
}
