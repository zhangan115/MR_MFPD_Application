package com.mr.mf_pd.application.view.main

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mr.mf_pd.application.common.Constants
import com.mr.mf_pd.application.repository.impl.UserRepository
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MainViewModel(private val userRepository: UserRepository) : ViewModel() {
    var toastStr: MutableLiveData<String> = MutableLiveData()

    fun start() {
        GlobalScope.launch {
            val list = ArrayList<Short>()
            for (index in 0 until Constants.PRPS_COLUMN) {
                for (value in Constants.indices) {
                    val short = value + (index * 8)
                    list.add(short.toShort())
                }
            }
            Constants.indicesList = list.toShortArray()
        }
    }
}