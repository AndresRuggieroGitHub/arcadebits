package com.example.arcadebits.utils


object ValidationUtils {

    fun isNonEmpty(value: String): Boolean =
        value.trim().isNotEmpty()


    fun isValidEmail(email: String): Boolean {
        val trimmed = email.trim()
        val regex = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()
        return regex.matches(trimmed)
    }

    fun isValidPassword(pass: String): Boolean =
        pass.length >= 6



    fun isMatching(a: String, b: String): Boolean =
        a == b


    fun isValidName(name: String): Boolean {
        val trimmed = name.trim()
        val pattern = "^[A-Za-zÁÉÍÓÚáéíóúÑñ ]{2,}$".toRegex()
        return pattern.matches(trimmed)
    }


    fun isPasswordComplex(pass: String): Boolean {
        val pattern = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{6,}$".toRegex()
        return pattern.matches(pass)
    }

}