package io.github.raghavsatyadev.support.core

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager.LayoutParams
import androidx.core.view.MenuHost
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle.State
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewbinding.ViewBinding
import io.github.raghavsatyadev.support.AppLog
import io.github.raghavsatyadev.support.databinding.LoaderBinding
import io.github.raghavsatyadev.support.extensions.AppExtensions.kotlinFileName
import io.github.raghavsatyadev.support.extensions.NavigationExtensions.getNavController
import io.github.raghavsatyadev.support.extensions.ViewExtensions.gone
import io.github.raghavsatyadev.support.extensions.ViewExtensions.hideKeyBoard
import io.github.raghavsatyadev.support.extensions.ViewExtensions.visible
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

abstract class CoreFragment<Binding : ViewBinding> : Fragment(), CoroutineScope {
    private var loader: LoaderBinding? = null
    lateinit var binding: Binding
    private lateinit var job: Job
    val currentNavID by lazy { getNavController().currentDestination?.id }
    lateinit var menuHost: MenuHost
    lateinit var coreActivity: CoreActivity<*>

    private val handler = CoroutineExceptionHandler { _, exception ->
        AppLog.loge(false, getClassName() + kotlinFileName, "mainHandler", exception, exception)
    }

    override val coroutineContext: CoroutineContext
        get() {
            var corContext = Dispatchers.Main + job
            if (!io.github.raghavsatyadev.support.BuildConfig.DEBUG) corContext += handler
            return corContext
        }

    val mainDispatcher = Dispatchers.Main
    val ioDispatcher = Dispatchers.IO
    val defaultDispatcher = Dispatchers.Default

    final override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        setupBinding(inflater, container, savedInstanceState)

        return binding.root
    }


    final override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        coreActivity = requireActivity() as CoreActivity<*>

        findProgressBar()
        createReference(view, savedInstanceState)

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(State.STARTED) {
                setListeners(true)
            }
        }
    }

    override fun onStop() {
        super.onStop()
        setListeners(false)
    }

    abstract fun createReference(view: View, savedInstanceState: Bundle?)

    private fun setupBinding(
        layoutInflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ) {
        binding = createBinding(layoutInflater, container, savedInstanceState)
    }

    abstract fun createBinding(
        layoutInflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): Binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        job = Job()
    }

    override fun onDestroy() {
        job.cancel()
        super.onDestroy()
    }

    override fun onDestroyView() {
        setListeners(false)
        super.onDestroyView()
    }

    abstract fun setListeners(isEnabled: Boolean)

    private fun getClassName(): String {
        return this::class.java.simpleName
    }

    private fun findProgressBar() {
        if (loader == null) {
            loader = getProgressBar()
        }
    }

    abstract fun getProgressBar(): LoaderBinding?

    fun showProgressBar() {
        setProgressBar(true)
    }

    fun hideProgressBar() {
        setProgressBar(false)
    }

    private fun setProgressBar(showProgressBar: Boolean) {
        if (showProgressBar) loader?.root?.visible() else loader?.root?.gone()

        setListeners(!showProgressBar)

        disableScreen(showProgressBar)
    }

    private fun disableScreen(disable: Boolean) {
        if (disable) {
            if (loader != null) loader?.root?.hideKeyBoard()
            requireActivity().window.setFlags(
                LayoutParams.FLAG_NOT_TOUCHABLE,
                LayoutParams.FLAG_NOT_TOUCHABLE
            )
        } else {
            requireActivity().window.clearFlags(LayoutParams.FLAG_NOT_TOUCHABLE)
        }
    }
}