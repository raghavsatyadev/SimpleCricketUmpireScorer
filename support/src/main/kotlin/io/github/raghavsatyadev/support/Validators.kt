package io.github.raghavsatyadev.support

import android.text.TextUtils
import android.util.Patterns
import androidx.core.util.PatternsCompat
import java.util.regex.Pattern

object Validators {
    fun isValidEmail(email: String): Boolean {
        return !TextUtils.isEmpty(email) && PatternsCompat.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun isValidMobile(phone: String): Boolean {
        return !TextUtils.isEmpty(phone) && Patterns.PHONE.matcher(phone).matches()
    }

    /**
     * Checks for 1 Uppercase Alphabet, 1 Number, 1 Special Character and at
     * least 8 character length
     *
     * @param password String password to validate
     * @return returns true if password is in correct format
     */
    fun isValidPassword(password: String?): Boolean {
        val passwordPattern = "^(?=.*\\d)(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$"
        return !TextUtils.isEmpty(password) && Pattern
            .compile(passwordPattern)
            .matcher(password?.trim()!!)
            .matches()

    }

}