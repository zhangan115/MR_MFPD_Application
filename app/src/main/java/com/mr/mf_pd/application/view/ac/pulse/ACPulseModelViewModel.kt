package com.mr.mf_pd.application.view.ac.pulse

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mr.mf_pd.application.model.UHFModelBean
import com.mr.mf_pd.application.repository.impl.DataRepository

class ACPulseModelViewModel(val repository: DataRepository) : ViewModel() {

    var toastStr: MutableLiveData<String> = MutableLiveData()
    var location: MutableLiveData<String> = MutableLiveData("/榆林有色220kV")

    fun start() {

    }

}