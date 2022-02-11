package com.mr.mf_pd.application.view.check.ac.flight

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mr.mf_pd.application.repository.impl.DataRepository

class ACFlightModelViewModel(val repository: DataRepository) : ViewModel() {

    var toastStr: MutableLiveData<String> = MutableLiveData()
    var location: MutableLiveData<String> = MutableLiveData("/榆林有色220kV")
    var timeStr: MutableLiveData<String> = MutableLiveData("12:09:23")
    var synchronizationModel: MutableLiveData<String> = MutableLiveData("内同步，50kHz-300kHz")
    var gainLevelStr: MutableLiveData<String> = MutableLiveData("20dB")

    lateinit var gainValues: MutableLiveData<List<Float>>

    fun start() {
        this.gainValues = repository.getGainValueList()
    }

}