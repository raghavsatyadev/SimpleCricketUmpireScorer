@file:OptIn(ExperimentalMaterial3Api::class)

package io.github.raghavsatyadev.library.support.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import io.github.raghavsatyadev.library.support.models.essential.ErrorCode
import org.jetbrains.compose.resources.stringResource
import scus.composeapp.generated.resources.Res
import scus.composeapp.generated.resources.okay

@Composable
fun ErrorDialog(errorCode: ErrorCode, errorMessage: String? = null, onDismiss: () -> Unit) {
  AlertDialog(
    onDismissRequest = { onDismiss() },
    text = { Text(text = errorMessage ?: errorCode.name) },
    confirmButton = {
      TextButton(onClick = { onDismiss() }) { Text(text = stringResource(Res.string.okay)) }
    },
  )
}
