package io.github.raghavsatyadev.scus.compose.ui.user

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun LoginScreen(viewModel: LoginScreenViewModel = hiltViewModel(), onLoginSuccess: () -> Unit) {
    if (viewModel.isLoggedIn()) {
        onLoginSuccess()
    } else {
        Scaffold(modifier = Modifier) { innerPadding ->
            Column(modifier = Modifier.padding(innerPadding)) {
                Text(text = stringResource(io.github.raghavsatyadev.scus.R.string.google_login))
            }
        }
    }
}
