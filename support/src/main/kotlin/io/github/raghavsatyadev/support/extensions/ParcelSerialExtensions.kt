package io.github.raghavsatyadev.support.extensions

import android.content.Intent
import android.os.Build
import android.os.Parcelable
import java.io.Serializable
import kotlin.reflect.KClass

@Suppress("unused")
object ParcelSerialExtensions {
    @Suppress("DEPRECATION")
    inline fun <reified T : Parcelable> Intent.getParcelExtra(key: String, kClass: KClass<T>) =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getParcelableExtra(key, kClass.java)
        } else {
            getParcelableExtra(key) as? T
        }

    @Suppress("DEPRECATION")
    inline fun <reified T : Serializable> Intent.getSerialExtra(key: String, kClass: KClass<T>) =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getSerializableExtra(key, kClass.java)
        } else {
            getSerializableExtra(key) as? T
        }
}