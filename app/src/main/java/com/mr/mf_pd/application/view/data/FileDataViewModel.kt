package com.mr.mf_pd.application.view.data

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mr.mf_pd.application.common.CheckType
import com.mr.mf_pd.application.model.CheckParamsBean
import com.mr.mf_pd.application.model.SettingBean
import com.mr.mf_pd.application.repository.impl.DataRepository
import com.mr.mf_pd.application.repository.impl.FilesRepository
import io.reactivex.disposables.Disposable
import java.io.File

class FileDataViewModel(var dataRepository: DataRepository, val fileRepository: FilesRepository) :
    ViewModel() {

    var toastStr: MutableLiveData<String> = MutableLiveData()
    private val disposableList = ArrayList<Disposable>()
    var settingBean: SettingBean? = null
    var checkParamsBean: MutableLiveData<CheckParamsBean>? = null
    val settingValues: ArrayList<Float> = ArrayList()
    lateinit var mCheckType: CheckType

    fun start(checkType: CheckType, file: File) {
        mCheckType = checkType
        dataRepository.setCheckType(checkType)
        fileRepository.setCurrentChickFile(file)
        checkParamsBean = checkType.checkParams
    }

    fun readDataFromFile(){
        fileRepository
    }
}