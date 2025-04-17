package io.github.raghavsatyadev.support.core

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

@HiltViewModel
open class CoreViewModel @Inject constructor() : ViewModel() {
  val mainDispatcher = Dispatchers.Main
  val ioDispatcher = Dispatchers.IO
  val defaultDispatcher = Dispatchers.Default
}
