package io.github.raghavsatyadev.support.extensions

import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.annotation.MenuRes
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle.State
import androidx.viewbinding.ViewBinding
import io.github.raghavsatyadev.support.core.CoreActivity
import io.github.raghavsatyadev.support.core.CoreFragment

@Suppress("MemberVisibilityCanBePrivate", "unused")
object MenuExtensions {
    fun <T : ViewBinding> CoreFragment<T>.invalidateOptionsMenu() {
        menuHost.invalidateMenu()
    }

    fun <T : ViewBinding> CoreFragment<T>.setupOptionsMenus(
        @MenuRes
        menuRes: Array<Int>,
        menuPrepareListener: ((Menu) -> Unit)? = null,
        onMenuItemClickListener: (MenuItem) -> Boolean,
    ) {
        menuHost = requireActivity()

        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuRes.forEach {
                    menuInflater.inflate(it, menu)
                }
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return onMenuItemClickListener(menuItem)
            }

            override fun onPrepareMenu(menu: Menu) {
                menuPrepareListener?.invoke(menu)
                super.onPrepareMenu(menu)
            }
        }, viewLifecycleOwner, State.STARTED)
    }

    fun <T : ViewBinding> CoreFragment<T>.setupOptionsMenus(
        @MenuRes
        menuRes: Int,
        menuPrepareListener: ((Menu) -> Unit)? = null,
        onMenuItemClickListener: (MenuItem) -> Boolean,
    ) {
        setupOptionsMenus(
            arrayOf(menuRes),
            onMenuItemClickListener = onMenuItemClickListener,
            menuPrepareListener = menuPrepareListener
        )
    }

    fun <T : ViewBinding> CoreActivity<T>.setupOptionsMenus(
        @MenuRes
        menuRes: Array<Int>,
        menuPrepareListener: ((Menu) -> Unit)? = null,
        onMenuItemClickListener: (MenuItem) -> Boolean,
    ) {
        addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuRes.forEach {
                    menuInflater.inflate(it, menu)
                }
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return onMenuItemClickListener(menuItem)
            }

            override fun onPrepareMenu(menu: Menu) {
                menuPrepareListener?.invoke(menu)
                super.onPrepareMenu(menu)
            }
        }, this, State.STARTED)
    }

    fun <T : ViewBinding> CoreActivity<T>.setupOptionsMenus(
        @MenuRes
        menuRes: Int,
        menuPrepareListener: ((Menu) -> Unit)? = null,
        onMenuItemClickListener: (MenuItem) -> Boolean,
    ) {
        setupOptionsMenus(arrayOf(menuRes), menuPrepareListener, onMenuItemClickListener)
    }
}