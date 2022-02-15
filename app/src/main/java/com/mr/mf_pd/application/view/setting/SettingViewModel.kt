package com.mr.mf_pd.application.view.setting

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mr.mf_pd.application.app.MRApplication
import com.mr.mf_pd.application.repository.impl.SettingRepository

class SettingViewModel(val setting: SettingRepository) : ViewModel() {

    var toastStr: MutableLiveData<String> = MutableLiveData()
    var linkIP: MutableLiveData<String> = MutableLiveData()
    var linkPort: MutableLiveData<String> = MutableLiveData()

    fun start() {
        val ip= MRApplication.instance.settingRepository.getLinkIP()
        linkIP.value =  ip
        val port = MRApplication.instance.settingRepository.getLinkPort()
        linkPort.value = port
    }

    override fun onCleared() {
        super.onCleared()
        setting.saveLinkIP(linkIP.value)
        setting.saveLinkPort(linkPort.value)
    }

}