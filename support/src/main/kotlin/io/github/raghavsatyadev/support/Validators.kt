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
        return !TextUtils.isEmpty(password) && Pattern.compile(passwordPattern)
            .matcher(password?.trim()!!)
            .matches()

    }

    /**
     * Checks for 1 Letter between A-E, a hyphen, 1-2 digits between 1-13, a
     * zero and 1 digit between 1-4
     */
    fun isValidFlatNumber(flatNumber: String?): Boolean {
        val pattern = "^[A-E]-([1-9]|1[0-3])0[1-4]$"
        return !TextUtils.isEmpty(flatNumber) && Pattern.compile(pattern.trim())
            .matcher(flatNumber!!)
            .matches()
    }

    /**
     * Checks number plate format through indian format of GJ03AB1234 or
     * 24BH1234AB
     *
     * @param numberPlate
     * @return true if number plate is in correct format
     */
    fun isValidNumberPlate(numberPlate: String): Boolean {
        val statePattern = "[A-Z]{2}[0-9]{2}[A-Z]{1,2}[0-9]{4}" // GJ03AB1234
        val bharatPattern = "[0-9]{2}BH[0-9]{4}[A-Z]{1,2}" // 24BH1234AB
        val pattern = "((?:$statePattern)|(?:$bharatPattern))"
        return !TextUtils.isEmpty(numberPlate) && Pattern.compile(pattern.trim())
            .matcher(numberPlate)
            .matches()
    }
}