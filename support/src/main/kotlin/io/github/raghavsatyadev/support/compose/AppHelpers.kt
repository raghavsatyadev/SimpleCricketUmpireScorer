package io.github.raghavsatyadev.support.compose

import android.app.Activity
import android.content.Context
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.Bullet
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp
import io.github.raghavsatyadev.support.compose.database.RoomDBComposeUtil
import io.github.raghavsatyadev.support.compose.google.FireStoreUtil
import io.github.raghavsatyadev.support.compose.google.FirebaseAuthUtil
import io.github.raghavsatyadev.support.extensions.ImplicitIntentExtensions.openPlayServiceUpdate
import io.github.raghavsatyadev.support.google.GoogleExtensions.checkPlayServiceAvailability
import io.github.raghavsatyadev.support.preferences.AppPrefsUtil

object AppHelpers {

  suspend fun signOut(
    fireStoreUtil: FireStoreUtil,
    authUtil: FirebaseAuthUtil,
    roomDBUtil: RoomDBComposeUtil,
    doSignOutFromFirestore: Boolean = false,
  ) {
    if (doSignOutFromFirestore) {
      fireStoreUtil.signOutUser()
    }
    roomDBUtil.deleteAll()
    AppPrefsUtil.clearAppPreferences()
    authUtil.signOut()
  }

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

  fun createdBulletedPointText(
    title: String,
    bulletPoints: List<String>,
  ): AnnotatedString {
    return buildAnnotatedString {
      withStyle(style = SpanStyle(fontSize = 16.sp)) { appendLine(title) }

      withBulletList {
        for (bulletPoint in bulletPoints) {
          withBulletListItem(bullet = Bullet.Default.copy(padding = 8.sp)) { append(bulletPoint) }
        }
      }
    }
  }
}
