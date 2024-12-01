package io.github.raghavsatyadev.support.core

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Dispatchers

open class CoreViewModel : ViewModel() {
    val mainDispatcher = Dispatchers.Main
    val ioDispatcher = Dispatchers.IO
    val defaultDispatcher = Dispatchers.Default
}