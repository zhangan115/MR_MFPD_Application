package com.mr.mf_pd.application.view.uhf.prps

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mr.mf_pd.application.repository.impl.DataRepository

class UHFPrPsViewModel(val repository: DataRepository) : ViewModel() {
    var toastStr: MutableLiveData<String> = MutableLiveData()
}