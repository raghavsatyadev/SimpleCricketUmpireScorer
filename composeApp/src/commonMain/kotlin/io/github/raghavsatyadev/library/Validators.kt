package io.github.raghavsatyadev.library

object Validators {
  fun isValidEmail(email: String): Boolean {
    // Basic email regex
    val emailRegex = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$"
    return email.isNotEmpty() && Regex(emailRegex).matches(email)
  }

  fun isValidMobile(phone: String): Boolean {
    // Basic phone validation (digits only, length 7-15)
    val phoneRegex = "^[0-9]{7,15}$"
    return phone.isNotEmpty() && Regex(phoneRegex).matches(phone)
  }

  /**
   * Checks for 1 Uppercase Alphabet, 1 Number, 1 Special Character and at least 8 character length
   *
   * @param password String password to validate
   * @return returns true if password is in correct format
   */
  fun isValidPassword(password: String?): Boolean {
    if (password.isNullOrEmpty()) return false
    val passwordPattern = "^(?=.*\\d)(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$"
    return Regex(passwordPattern).matches(password.trim())
  }
}
