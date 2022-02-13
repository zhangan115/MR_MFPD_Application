package com.mr.mf_pd.application.view.check.uhf

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mr.mf_pd.application.common.CheckType
import com.mr.mf_pd.application.manager.socket.CommandHelp
import com.mr.mf_pd.application.manager.socket.CommandType
import com.mr.mf_pd.application.manager.socket.SocketManager
import com.mr.mf_pd.application.repository.impl.DataRepository
import com.mr.mf_pd.application.repository.impl.SettingRepository

class CheckUHFViewModel(
    private val dataRepository: DataRepository,
    private val settingRepository: SettingRepository,
) : ViewModel() {

    var toastStr: MutableLiveData<String> = MutableLiveData()

    fun start(checkType: CheckType) {
        settingRepository.getSettingData(checkType)
        dataRepository.setCheckType(checkType)
        dataRepository.switchPassageway(checkType.type)
    }

    override fun onCleared() {
        super.onCleared()
        dataRepository.closePassageway()
    }
}