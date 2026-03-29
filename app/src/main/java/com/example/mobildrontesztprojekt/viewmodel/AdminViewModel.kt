package com.example.mobildrontesztprojekt.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobildrontesztprojekt.data.database.AppDatabase
import com.example.mobildrontesztprojekt.data.entity.Company
import com.example.mobildrontesztprojekt.data.entity.User
import kotlinx.coroutines.launch

class AdminViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)

    val users = db.userDao().getAll()
    val companies = db.companyDao().getAll()

    fun insertUser(user: User) = viewModelScope.launch { db.userDao().insert(user) }
    fun updateUser(user: User) = viewModelScope.launch { db.userDao().update(user) }
    fun deleteUser(user: User) = viewModelScope.launch { db.userDao().delete(user) }

    fun insertCompany(company: Company) = viewModelScope.launch { db.companyDao().insert(company) }
    fun updateCompany(company: Company) = viewModelScope.launch { db.companyDao().update(company) }
    fun deleteCompany(company: Company) = viewModelScope.launch { db.companyDao().delete(company) }

    fun hashPassword(password: String) = AppDatabase.hashPassword(password)
}
