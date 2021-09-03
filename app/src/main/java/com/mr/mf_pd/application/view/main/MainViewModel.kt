package com.mr.mf_pd.application.view.main

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mr.mf_pd.application.repository.impl.UserRepository

class MainViewModel(private val userRepository: UserRepository) : ViewModel() {
    var toastStr: MutableLiveData<String> = MutableLiveData()
    var appName: MutableLiveData<String> = MutableLiveData()
}