package io.github.raghavsatyadev.library.support.extensions

import platform.Foundation.NSString
import platform.Foundation.stringWithFormat

actual fun Double.format(digits: Int): String = NSString.stringWithFormat("%.${digits}f", this)
