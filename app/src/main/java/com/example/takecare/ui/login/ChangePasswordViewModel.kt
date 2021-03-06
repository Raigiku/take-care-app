package com.example.takecare.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.takecare.data.api.OperationResult
import com.example.takecare.data.api.response.RecoverPasswordResponse
import com.example.takecare.data.repository.RecoverPasswordRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChangePasswordViewModel(private val repository: RecoverPasswordRepository) : ViewModel() {
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _isRequestSuccess = MutableLiveData<Boolean>()
    val isRequestSuccess: LiveData<Boolean> = _isRequestSuccess

    private val _onMessageError= MutableLiveData<Any>()
    val onMessageError: LiveData<Any> = _onMessageError

    fun changePassword(mail: String, password: String, token:String) {
        _isLoading.postValue(true)
        viewModelScope.launch {
            val result: OperationResult<RecoverPasswordResponse> = withContext(Dispatchers.IO) {
                repository.recoverPassword(mail, password, token)
            }
            _isLoading.postValue(false)
            when (result) {
                is OperationResult.Success -> {
                    _isRequestSuccess.postValue(true)
                }
                is OperationResult.Error -> {
                    _isRequestSuccess.postValue(false)
                    _onMessageError.postValue(result.exception?.message)
                }
            }
        }
    }
}