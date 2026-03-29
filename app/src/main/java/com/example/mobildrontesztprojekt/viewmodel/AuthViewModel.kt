package com.example.mobildrontesztprojekt.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobildrontesztprojekt.data.database.AppDatabase
import com.example.mobildrontesztprojekt.data.entity.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    private val _loginError = MutableStateFlow<String?>(null)
    val loginError: StateFlow<String?> = _loginError

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _loginError.value = "Az email és jelszó megadása kötelező"
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            _loginError.value = null
            val hash = AppDatabase.hashPassword(password)
            val user = db.userDao().findByCredentials(email.trim(), hash)
            if (user != null) {
                _currentUser.value = user
            } else {
                _loginError.value = "Hibás email cím vagy jelszó"
            }
            _isLoading.value = false
        }
    }

    fun logout() {
        _currentUser.value = null
        _loginError.value = null
    }

    fun clearError() {
        _loginError.value = null
    }
}
