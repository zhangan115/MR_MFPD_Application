package com.mr.mf_pd.application.view.check.activity

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mr.mf_pd.application.common.CheckType
import com.mr.mf_pd.application.model.CheckParamsBean
import com.mr.mf_pd.application.model.SettingBean
import com.mr.mf_pd.application.repository.impl.DataRepository
import com.mr.mf_pd.application.repository.impl.SettingRepository
import io.reactivex.disposables.Disposable
import java.io.File

class CheckDataViewModel(val dataRepository: DataRepository, val settingRepository: SettingRepository):ViewModel() {

    var writeSetting = false
    var writeSettingCommand: ByteArray? = null
    var toastStr: MutableLiveData<String> = MutableLiveData()
    private val disposableList = ArrayList<Disposable>()
    var settingBean: SettingBean? = null
    var checkParamsBean: MutableLiveData<CheckParamsBean>? = null
    val settingValues: ArrayList<Float> = ArrayList()
    private var disposable: Disposable? = null
    lateinit var mCheckType: CheckType
    var ycByteArray: ByteArray? = null

    fun start(checkType: CheckType,file:File) {
        mCheckType = checkType
        checkParamsBean = checkType.checkParams
        dataRepository.setCheckType(checkType)
        dataRepository.readDataFromFile(file)
    }
}