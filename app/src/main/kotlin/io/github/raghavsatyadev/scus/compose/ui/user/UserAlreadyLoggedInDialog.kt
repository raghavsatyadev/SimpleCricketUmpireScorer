package io.github.raghavsatyadev.scus.compose.ui.user

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import io.github.raghavsatyadev.scus.R
import io.github.raghavsatyadev.support.compose.AppComposeExtensions.AlreadyLoggedInText
import io.github.raghavsatyadev.support.compose.components.LightPreview
import io.github.raghavsatyadev.support.R as Rs

@Composable
fun UserAlreadyLoggedInDialog(onForceLogin: () -> Unit, onSignOut: () -> Unit) {
  AlertDialog(
    confirmButton = {
      TextButton(onClick = { onForceLogin.invoke() }) { Text(stringResource(R.string.force_login)) }
    },
    dismissButton = {
      TextButton(onClick = { onSignOut.invoke() }) { Text(stringResource(R.string.logout)) }
    },
    title = { Text(stringResource(Rs.string.dialog_already_logged_in_title)) },
    text = { Text(AlreadyLoggedInText()) },
    onDismissRequest = {},
  )
}

@LightPreview
@Composable
private fun UserAlreadyLoggedInDialogPreview() {
  UserAlreadyLoggedInDialog(onForceLogin = {}, onSignOut = {})
}
