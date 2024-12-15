package io.github.raghavsatyadev.scuc.ui.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.github.raghavsatyadev.scuc.databinding.ActivityLoginBinding
import io.github.raghavsatyadev.scuc.databinding.DialogAlreadyLoggedInBinding
import io.github.raghavsatyadev.support.R
import io.github.raghavsatyadev.support.core.CoreActivity
import io.github.raghavsatyadev.support.extensions.ErrorShowExtensions.errorDialog
import io.github.raghavsatyadev.support.google.GoogleSignInUtil
import io.github.raghavsatyadev.support.models.essential.Resource
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginActivity : CoreActivity<ActivityLoginBinding>() {
    private val viewModel: LoginViewModel by viewModels()
    private lateinit var signInUtil: GoogleSignInUtil

    companion object {
        fun getIntentObject(
            context: Context,
            bundle: Bundle = Bundle.EMPTY,
        ): Intent = Intent(
            context,
            LoginActivity::class.java
        ).apply { putExtras(bundle) }
    }


    override fun createReference(savedInstanceState: Bundle?) {
        signInUtil = GoogleSignInUtil(this)
    }

    private fun startSignIn() {
        lifecycleScope.launch {
            viewModel.signInWithGoogle(signInUtil)
            viewModel
                .getLoginEvent()
                .collectLatest { value ->
                    withContext(mainDispatcher) {
                        when (value.status) {
                            Resource.Status.LOADING -> {
                                showProgressBar()
                            }

                            Resource.Status.SUCCESS -> {
                                hideProgressBar()
                                when (value.data) {
                                    LoginState.SUCCESS -> {
                                        setResult(RESULT_OK)
                                        finish()
                                    }

                                    LoginState.USER_ALREADY_LOGGED_IN -> {
                                        showUserAlreadyLoggedInDialog()
                                    }

                                    else -> {
                                        errorDialog(R.string.warning_unknown_error)
                                        setResult(RESULT_CANCELED)
                                        finish()
                                    }
                                }
                            }

                            Resource.Status.ERROR -> {
                                hideProgressBar()
                                val errorDialog = errorDialog(R.string.warning_unknown_error)
                                errorDialog?.setOnDismissListener { _ ->
                                    setResult(RESULT_CANCELED)
                                    finish()
                                }
                            }

                            Resource.Status.EMPTY -> {
                                hideProgressBar()
                            }
                        }
                    }
                }
        }
    }

    private fun showUserAlreadyLoggedInDialog() {
        val dialogBinding = DialogAlreadyLoggedInBinding.inflate(layoutInflater)
        val dialog = MaterialAlertDialogBuilder(this)
            .setView(dialogBinding.root)
            .setCancelable(false)
            .show()
        dialogBinding.btnForceLogin.setOnClickListener {
            lifecycleScope.launch {
                withContext(mainDispatcher) {
                    dialog.dismiss()
                }
                viewModel.updateUserTokens()
            }
        }
        dialogBinding.btnLogout.setOnClickListener {
            lifecycleScope.launch {
                viewModel.signOut(signInUtil)
                withContext(mainDispatcher) {
                    dialog.dismiss()
                    finishAffinity()
                }
            }
        }
    }

    override fun getProgressBar() = binding.loader

    override fun createBinding(savedInstanceState: Bundle?) =
        ActivityLoginBinding.inflate(layoutInflater)

    override fun setListeners(isEnabled: Boolean) {
        if (isEnabled) {
            binding.btnGoogleLogin.setOnClickListener {
                startSignIn()
            }
        } else {
            binding.btnGoogleLogin.setOnClickListener(null)
        }
    }
}