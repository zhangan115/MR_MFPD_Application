package com.mr.mf_pd.application.view.check.pulse

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mr.mf_pd.application.common.CheckType
import com.mr.mf_pd.application.repository.impl.DataRepository
import com.mr.mf_pd.application.repository.impl.FilesRepository
import java.util.*

class ACPulseModelViewModel(val dataRepository: DataRepository, val filesRepository: FilesRepository) : ViewModel() {

    var toastStr: MutableLiveData<String> = MutableLiveData()
    var location: MutableLiveData<String> = MutableLiveData("/榆林有色220kV")
    var timeStr: MutableLiveData<String> = MutableLiveData("12:09:23")
    var synchronizationModel: MutableLiveData<String> = MutableLiveData("内同步，50kHz-300kHz")
    var gainLevelStr: MutableLiveData<String> = MutableLiveData("20dB")
    var gainMinValue: MutableLiveData<Float?> = MutableLiveData()

    lateinit var checkType: CheckType

    var gainValues: MutableLiveData<Vector<Float>> = MutableLiveData(Vector<Float>())
    var isFile: MutableLiveData<Boolean> = MutableLiveData(false)

    fun start() {
        if (isFile.value!!) {
            this.checkType = filesRepository.getCheckType()
        } else {
            this.checkType = dataRepository.getCheckType()
        }
    }

    fun onResume() {

    }

    fun onPause() {

    }

}