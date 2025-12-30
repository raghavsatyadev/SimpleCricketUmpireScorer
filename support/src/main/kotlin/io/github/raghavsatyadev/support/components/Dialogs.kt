@file:OptIn(ExperimentalMaterial3Api::class)

package io.github.raghavsatyadev.support.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import io.github.raghavsatyadev.support.models.essential.ErrorCode
import io.github.raghavsatyadev.support.R as Rs

@Composable
fun ErrorDialog(errorCode: ErrorCode, errorMessage: String? = null, onDismiss: () -> Unit) {
  AlertDialog(
    onDismissRequest = { onDismiss() },
    text = { Text(text = errorMessage ?: errorCode.name) },
    confirmButton = {
      TextButton(onClick = { onDismiss() }) { Text(text = stringResource(Rs.string.okay)) }
    },
  )
}
