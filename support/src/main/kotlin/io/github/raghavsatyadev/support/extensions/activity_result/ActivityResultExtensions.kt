package io.github.raghavsatyadev.support.extensions.activity_result

import android.content.Intent
import android.content.IntentSender
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.IntentSenderRequest.Builder
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.activity.result.contract.ActivityResultContracts.StartIntentSenderForResult
import androidx.core.app.ActivityOptionsCompat
import androidx.fragment.app.Fragment

@Suppress("unused")
object ActivityResultExtensions {
    fun ComponentActivity.registerActivityForResult(
        listener: ActivityResultListener,
    ): ActivityResultLauncher<Intent> {
        return registerForActivityResult(StartActivityForResult()) { result: ActivityResult ->
            listener.onResult(ResultType.getEnumByResultIndex(result.resultCode), result.data)
        }
    }

    fun ComponentActivity.registerActivityForResultSender(
        listener: ActivityResultListener,
    ): ActivityResultLauncher<IntentSenderRequest> {
        return registerForActivityResult(StartIntentSenderForResult()) { result: ActivityResult ->
            listener.onResult(ResultType.getEnumByResultIndex(result.resultCode), result.data)
        }
    }

    fun ActivityResultLauncher<IntentSenderRequest>.launchSender(
        intent: IntentSender,
        optionsCompat: ActivityOptionsCompat? = null,
    ) {
        launch(Builder(intent).build(), optionsCompat)
    }

    fun Fragment.registerActivityForResult(
        listener: ActivityResultListener,
    ): ActivityResultLauncher<Intent> {
        return registerForActivityResult(StartActivityForResult()) { result: ActivityResult ->
            listener.onResult(ResultType.getEnumByResultIndex(result.resultCode), result.data)
        }
    }

    fun Fragment.registerActivityForResultSender(
        listener: ActivityResultListener,
    ): ActivityResultLauncher<IntentSenderRequest> {
        return registerForActivityResult(StartIntentSenderForResult()) { result: ActivityResult ->
            listener.onResult(ResultType.getEnumByResultIndex(result.resultCode), result.data)
        }
    }
}