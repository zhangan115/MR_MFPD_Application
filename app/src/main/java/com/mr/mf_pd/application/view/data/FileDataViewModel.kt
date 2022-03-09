package com.mr.mf_pd.application.view.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mr.mf_pd.application.common.CheckType
import com.mr.mf_pd.application.manager.file.ReadFileManager
import com.mr.mf_pd.application.manager.socket.callback.BaseDataCallback
import com.mr.mf_pd.application.model.CheckParamsBean
import com.mr.mf_pd.application.model.Event
import com.mr.mf_pd.application.model.SettingBean
import com.mr.mf_pd.application.repository.impl.DataRepository
import com.mr.mf_pd.application.repository.impl.FilesRepository
import io.reactivex.disposables.Disposable
import java.io.File

class FileDataViewModel(var dataRepository: DataRepository, private val fileRepository: FilesRepository) :
    ViewModel() {

    var toastStr: MutableLiveData<String> = MutableLiveData()
    var settingBean: SettingBean? = null
    var checkParamsBean: MutableLiveData<CheckParamsBean>? = null
    val settingValues: ArrayList<Float> = ArrayList()
    lateinit var mCheckType: CheckType

    private var disposable: Disposable? = null

    fun start(checkType: CheckType, file: File) {
        mCheckType = checkType
        dataRepository.setCheckType(checkType)
        val checkDir = file.parentFile
        if (checkDir != null) {
            fileRepository.setCurrentClickFile(checkDir)
        }
        checkParamsBean = checkType.checkParams
        readYcValue()
        fileRepository.addDataListener()
        ReadFileManager.get().setFile(file)
        ReadFileManager.get().startReadReadData()
    }

    private val _toYcDataEvent = MutableLiveData<Event<ByteArray>>()
    val toYcDataEvent: LiveData<Event<ByteArray>> = _toYcDataEvent

    private fun readYcValue() {
        fileRepository.addYcDataCallback(object : BaseDataCallback {
            override fun onData(source: ByteArray) {
                _toYcDataEvent.postValue(Event(source))
            }
        })
    }

    override fun onCleared() {
        super.onCleared()
        disposable?.dispose()
        disposable = null
    }
}