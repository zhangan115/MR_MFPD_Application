package com.mr.mf_pd.application.view.check.ac.flight

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mr.mf_pd.application.repository.impl.DataRepository

class ACFlightModelViewModel(val repository: DataRepository) : ViewModel() {

    var toastStr: MutableLiveData<String> = MutableLiveData()
    var location: MutableLiveData<String> = MutableLiveData("/榆林有色220kV")

    fun start() {

    }

}