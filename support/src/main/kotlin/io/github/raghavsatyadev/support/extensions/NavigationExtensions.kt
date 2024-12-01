package io.github.raghavsatyadev.support.extensions

import android.os.Bundle
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.AnimBuilder
import androidx.navigation.NavAction
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import androidx.navigation.NavGraph
import androidx.navigation.NavOptions
import androidx.navigation.Navigator
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.navOptions
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.viewbinding.ViewBinding
import io.github.raghavsatyadev.support.R
import io.github.raghavsatyadev.support.core.CoreActivity
import io.github.raghavsatyadev.support.core.CoreFragment

@Suppress("unused", "MemberVisibilityCanBePrivate")
object NavigationExtensions {
    private fun getAnimationNavOption() = navOptions {
        anim {
            getNavigationAnimationBuilder()
        }
    }

    private fun getNavigationAnimationBuilder() {
        AnimBuilder().apply {
            enter = R.anim.slide_in_right
            exit = R.anim.slide_out_left
            popEnter = R.anim.slide_in_left
            popExit = R.anim.slide_out_right
        }
    }

    fun NavController.navigateWithAnimation(
        @IdRes
        resId: Int,
        args: Bundle? = null,
        navOptions: NavOptions? = null,
        navExtras: Navigator.Extras? = null,
    ) {
        navigate(resId, args, navOptions ?: getAnimationNavOption(), navExtras)
    }

    fun NavController.navigateAction(
        action: NavAction,
        bundle: Bundle? = null,
    ) {
        navigateWithAnimation(action.destinationId, bundle, action.navOptions)
    }

    fun NavController.navigateInitial(
        @IdRes
        destinationId: Int,
        bundle: Bundle? = null,
    ) {
        val navOptions = navOptions {
            popUpTo(0) {
                inclusive = true
            }
        }
        navigateWithAnimation(destinationId, bundle, navOptions)
    }

    fun <V : ViewBinding> CoreFragment<V>.navigateToFragmentId(
        @IdRes
        id: Int,
    ) {

        if (getNavController().currentDestination?.id == currentNavID) {
            getNavController().navigate(id)
        }
    }

    fun <V : ViewBinding> CoreFragment<V>.navigateAction(
        action: NavDirections,
        hideBottomNav: Boolean = true,
    ) {
        (requireActivity() as CoreActivity<*>).setBottomNavVisibility(hideBottomNav)
        if (getNavController().currentDestination?.id == currentNavID) {
            getNavController().navigate(action)
        }
    }

    fun Fragment.navigateUp() = getNavController().navigateUp()

    fun Fragment.getNavController() = NavHostFragment.findNavController(this)

    fun AppCompatActivity.findActivityNavController(
        @IdRes
        navHostContainerID: Int,
    ) =
        (supportFragmentManager.findFragmentById(navHostContainerID) as NavHostFragment).navController

    fun AppCompatActivity.setupAppBarConfiguration(
        navGraph: NavGraph, navController: NavController,
    ): AppBarConfiguration {
        val appBarConfiguration = AppBarConfiguration.Builder(navGraph).build()
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration)
        return appBarConfiguration
    }
}