package io.github.raghavsatyadev.support.list

import android.view.View

open class CustomClickListener(
    private val onClick: (position: Int, v: View?, isLongClick: Boolean) -> Unit,
) {
    fun onItemClick(position: Int, v: View?, isLongClick: Boolean = false) {
        onClick(position, v, isLongClick)
    }
}