package com.computerroom.monitoring.viewmodel

import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException

class LoginViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()

    private val _loginResult = MutableLiveData<Boolean>()
    val loginResult: LiveData<Boolean> = _loginResult

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    private val _emailError = MutableLiveData<String?>()
    val emailError: LiveData<String?> = _emailError

    private val _passwordError = MutableLiveData<String?>()
    val passwordError: LiveData<String?> = _passwordError

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private fun validateEmail(email: String): Boolean {
        return when {
            email.isBlank() -> {
                _emailError.value = "Vui long nhap email"
                false
            }
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                _emailError.value = "Email khong dung dinh dang (vd: user@email.com)"
                false
            }
            else -> {
                _emailError.value = null
                true
            }
        }
    }

    private fun validatePassword(password: String, isRegister: Boolean): Boolean {
        return when {
            password.isBlank() -> {
                _passwordError.value = "Vui long nhap mat khau"
                false
            }
            isRegister && password.length < 6 -> {
                _passwordError.value = "Mat khau phai co it nhat 6 ky tu"
                false
            }
            else -> {
                _passwordError.value = null
                true
            }
        }
    }

    private fun getFirebaseErrorMessage(exception: Exception?): String {
        return when (exception) {
            is FirebaseAuthInvalidCredentialsException ->
                "Email hoac mat khau khong dung. Vui long kiem tra lai."
            is FirebaseAuthInvalidUserException ->
                "Tai khoan khong ton tai. Vui long dang ky tai khoan moi."
            is FirebaseAuthUserCollisionException ->
                "Email nay da duoc dang ky. Vui long dang nhap hoac su dung email khac."
            is FirebaseAuthWeakPasswordException ->
                "Mat khau qua yeu. Vui long su dung mat khau manh hon (it nhat 6 ky tu)."
            else -> exception?.localizedMessage ?: "Da xay ra loi. Vui long thu lai."
        }
    }

    fun login(email: String, password: String) {
        val isEmailValid = validateEmail(email)
        val isPasswordValid = validatePassword(password, false)
        if (!isEmailValid || !isPasswordValid) return

        _isLoading.value = true
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                _isLoading.value = false
                if (task.isSuccessful) {
                    _loginResult.value = true
                } else {
                    _errorMessage.value = getFirebaseErrorMessage(task.exception)
                }
            }
    }

    fun register(email: String, password: String) {
        val isEmailValid = validateEmail(email)
        val isPasswordValid = validatePassword(password, true)
        if (!isEmailValid || !isPasswordValid) return

        _isLoading.value = true
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                _isLoading.value = false
                if (task.isSuccessful) {
                    _loginResult.value = true
                } else {
                    _errorMessage.value = getFirebaseErrorMessage(task.exception)
                }
            }
    }

    fun isLoggedIn(): Boolean {
        return auth.currentUser != null
    }
}
