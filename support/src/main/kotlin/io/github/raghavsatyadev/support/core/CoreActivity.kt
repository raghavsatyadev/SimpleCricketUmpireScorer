package io.github.raghavsatyadev.support.core

import android.os.Bundle
import android.view.MenuItem
import android.view.WindowManager
import androidx.activity.addCallback
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding
import io.github.raghavsatyadev.support.AppLog
import io.github.raghavsatyadev.support.databinding.LoaderBinding
import io.github.raghavsatyadev.support.databinding.ToolBarBinding
import io.github.raghavsatyadev.support.extensions.AppExtensions.kotlinFileName
import io.github.raghavsatyadev.support.extensions.OrientationExtensions.lockOrientation
import io.github.raghavsatyadev.support.extensions.ViewExtensions.gone
import io.github.raghavsatyadev.support.extensions.ViewExtensions.hideKeyBoard
import io.github.raghavsatyadev.support.extensions.ViewExtensions.visible
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext


abstract class CoreActivity<Binding : ViewBinding> : AppCompatActivity(), CoroutineScope {
    private lateinit var job: Job
    lateinit var binding: Binding
    private var loader: LoaderBinding? = null

    private val handler = CoroutineExceptionHandler { _, exception ->
        AppLog.loge(
            false,
            getClassName() + kotlinFileName,
            "mainHandler",
            exception,
            exception
        )
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

    abstract fun createReference(savedInstanceState: Bundle?)

    private fun setupBinding(savedInstanceState: Bundle?) {
        binding = createBinding(savedInstanceState)
    }

    open fun setBottomNavVisibility(hideBottomNav: Boolean) {

    }

    abstract fun createBinding(savedInstanceState: Bundle?): Binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        job = Job()
        lockOrientation()

        setupBinding(savedInstanceState)
        setContentView(binding.root)
        findProgressBar()
        setupToolBar()
        createReference(savedInstanceState)
        registerBackButton()
    }

    override fun onStart() {
        super.onStart()
        setupBackButton()
    }

    private fun setupToolBar() {
        setSupportActionBar(getToolBar()?.toolBar)
    }

    private fun setupBackButton() {
        if (!isTaskRoot) {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }
    }

    fun setToolBarTitle(title: String) {
        supportActionBar?.title = title
    }

    fun setToolBarTitle(
        @StringRes
        title: Int,
    ) {
        supportActionBar?.title = getString(title)
    }

    override fun onOptionsItemSelected(
        item: MenuItem,
    ): Boolean {
        if (item.itemId == android.R.id.home) {
            pressBack()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    open fun getToolBar(): ToolBarBinding? {
        return null
    }

    private fun findProgressBar() {
        if (loader == null) {
            loader = getProgressBar()
        }
    }

    open fun getProgressBar(): LoaderBinding? {
        return null
    }

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
            window.setFlags(
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
            )
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        }
    }

    private fun registerBackButton() {
        onBackPressedDispatcher.addCallback { onBackPressedCompat() }
    }

    open fun onBackPressedCompat() {
        finish()
    }

    fun pressBack() {
        onBackPressedDispatcher.onBackPressed()
    }

    override fun onResume() {
        super.onResume()
        setListeners(true)
    }

    override fun onPause() {
        setListeners(false)
        super.onPause()
    }

    override fun onDestroy() {
        job.cancel()
        super.onDestroy()
    }

    abstract fun setListeners(isEnabled: Boolean)

    private fun getClassName(): String {
        return this::class.java.simpleName
    }
}