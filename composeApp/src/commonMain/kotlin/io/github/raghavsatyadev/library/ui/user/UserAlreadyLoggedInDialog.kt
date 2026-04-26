package io.github.raghavsatyadev.library.ui.user

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import io.github.raghavsatyadev.library.support.components.DarkPreview
import io.github.raghavsatyadev.library.support.components.LightPreview
import io.github.raghavsatyadev.library.support.theme.AppTheme
import org.jetbrains.compose.resources.stringResource
import scus.composeapp.generated.resources.Res
import scus.composeapp.generated.resources.dialog_already_logged_in_title
import scus.composeapp.generated.resources.force_login
import scus.composeapp.generated.resources.logout
import scus.composeapp.generated.resources.warning_already_logged_in_1
import scus.composeapp.generated.resources.warning_already_logged_in_2
import scus.composeapp.generated.resources.warning_already_logged_in_3

@Composable
fun UserAlreadyLoggedInDialog(onForceLogin: () -> Unit, onSignOut: () -> Unit) {
  AlertDialog(
    confirmButton = {
      TextButton(onClick = { onForceLogin.invoke() }) {
        Text(stringResource(Res.string.force_login))
      }
    },
    dismissButton = {
      TextButton(onClick = { onSignOut.invoke() }) { Text(stringResource(Res.string.logout)) }
    },
    title = { Text(stringResource(Res.string.dialog_already_logged_in_title)) },
    text = {
      // TODO migrate createdBulletedPointText from AppHelpers
      Text(
        stringResource(Res.string.warning_already_logged_in_1) +
          "\n" +
          "- " +
          stringResource(Res.string.warning_already_logged_in_2) +
          "\n" +
          "- " +
          stringResource(Res.string.warning_already_logged_in_3)
      )
    },
    onDismissRequest = {},
  )
}

@DarkPreview
@LightPreview
@Composable
private fun UserAlreadyLoggedInDialogPreview() {
  AppTheme { UserAlreadyLoggedInDialog(onForceLogin = {}, onSignOut = {}) }
}
