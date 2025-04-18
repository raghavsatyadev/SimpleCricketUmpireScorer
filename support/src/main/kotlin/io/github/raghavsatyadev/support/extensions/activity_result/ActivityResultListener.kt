package io.github.raghavsatyadev.support.extensions.activity_result

import android.content.Intent

fun interface ActivityResultListener {
    fun onResult(resultType: ResultType, data: Intent?)
}
