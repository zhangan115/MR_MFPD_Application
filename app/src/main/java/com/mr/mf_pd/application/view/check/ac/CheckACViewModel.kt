package com.mr.mf_pd.application.view.check.ac

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mr.mf_pd.application.repository.impl.DataRepository

class CheckACViewModel(val dataRepository: DataRepository) : ViewModel() {

    var currentIndex: MutableLiveData<Int> = MutableLiveData(0)

    var toastStr: MutableLiveData<String> = MutableLiveData()

    fun start(){
        dataRepository.switchPassageway(2)
    }

}