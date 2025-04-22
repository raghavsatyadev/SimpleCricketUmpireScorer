package io.github.raghavsatyadev.support.compose

import android.app.Activity
import android.content.Context
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextIndent
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp
import io.github.raghavsatyadev.support.compose.google.FireStoreUtil
import io.github.raghavsatyadev.support.compose.google.FirebaseAuthUtil
import io.github.raghavsatyadev.support.database.RoomDBUtil
import io.github.raghavsatyadev.support.extensions.ImplicitIntentExtensions.openPlayServiceUpdate
import io.github.raghavsatyadev.support.google.GoogleExtensions.checkPlayServiceAvailability
import io.github.raghavsatyadev.support.preferences.AppPrefsUtil
import io.github.raghavsatyadev.support.R as Rs

object AppComposeExtensions {

  suspend fun signOut(
    fireStoreUtil: FireStoreUtil,
    authUtil: FirebaseAuthUtil,
    doSignOutFromFirestore: Boolean = false,
  ) {
    if (doSignOutFromFirestore) {
      fireStoreUtil.signOutUser()
    }
    authUtil.signOut()
    RoomDBUtil.deleteAll()
    AppPrefsUtil.clearAppPreferences()
  }

  @Composable fun finishAffinity() = LocalActivity.current?.finishAffinity()

  @Composable fun activity(): Activity? = LocalActivity.current

  @Composable fun context(): Context = LocalContext.current

  @Composable
  fun CheckPlayService(content: @Composable () -> Unit) {
    if (context().checkPlayServiceAvailability()) {
      content()
    } else {
      context().openPlayServiceUpdate()
    }
  }

  @Composable
  fun AlreadyLoggedInText(): AnnotatedString {
    val title = stringResource(Rs.string.warning_already_logged_in_1)
    val bulletPoint1 = stringResource(Rs.string.warning_already_logged_in_2)
    val bulletPoint2 = stringResource(Rs.string.warning_already_logged_in_3)

    return buildAnnotatedString {
      withStyle(style = SpanStyle(fontSize = 16.sp)) { appendLine(title) }

      withStyle(style = ParagraphStyle(textIndent = TextIndent(firstLine = 14.sp))) {
        appendLine("• $bulletPoint1")
      }
      withStyle(style = ParagraphStyle(textIndent = TextIndent(firstLine = 16.sp))) {
        append("• $bulletPoint2")
      }
    }
  }
}
