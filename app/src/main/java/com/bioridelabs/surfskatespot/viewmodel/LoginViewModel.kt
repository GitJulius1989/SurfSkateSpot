package com.bioridelabs.surfskatespot.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bioridelabs.surfskatespot.repository.UserRepository
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {

    val userRepository = UserRepository()

    // LiveData para comunicar el resultado del login a la vista
    private val _loginResult = MutableLiveData<Boolean>()
    val loginResult: LiveData<Boolean> get() = _loginResult

    fun login(email: String, password: String) {
        viewModelScope.launch {
            val result = userRepository.loginUser(email, password)
            _loginResult.value = result
        }
    }
}
