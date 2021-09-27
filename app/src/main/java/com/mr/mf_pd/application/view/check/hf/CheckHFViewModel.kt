package com.mr.mf_pd.application.view.check.hf

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mr.mf_pd.application.repository.impl.DataRepository

class CheckHFViewModel(dataRepository: DataRepository) : ViewModel() {

    var currentIndex: MutableLiveData<Int> = MutableLiveData(0)

    var toastStr: MutableLiveData<String> = MutableLiveData()

}