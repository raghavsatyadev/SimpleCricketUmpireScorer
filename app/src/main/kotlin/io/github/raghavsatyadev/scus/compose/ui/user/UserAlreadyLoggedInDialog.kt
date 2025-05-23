package io.github.raghavsatyadev.scus.compose.ui.user

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import io.github.raghavsatyadev.scus.R
import io.github.raghavsatyadev.support.compose.AppHelpers.createdBulletedPointText
import io.github.raghavsatyadev.support.compose.components.DarkPreview
import io.github.raghavsatyadev.support.compose.components.LightPreview
import io.github.raghavsatyadev.support.compose.theme.AppTheme
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
    text = {
      Text(
        createdBulletedPointText(
          stringResource(Rs.string.warning_already_logged_in_1),
          listOf(
            stringResource(Rs.string.warning_already_logged_in_2),
            stringResource(Rs.string.warning_already_logged_in_3),
          ),
        )
      )
    },
    onDismissRequest = {},
  )
}

@DarkPreview
@LightPreview
@Composable
private fun UserAlreadyLoggedInDialogPreview() {
  AppTheme {
    UserAlreadyLoggedInDialog(
      onForceLogin = {},
      onSignOut = {})
  }
}
