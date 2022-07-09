package com.mr.mf_pd.application.view.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mr.mf_pd.application.common.CheckType
import com.mr.mf_pd.application.manager.file.CheckFileReadManager
import com.mr.mf_pd.application.manager.socket.callback.BytesDataCallback
import com.mr.mf_pd.application.manager.socket.comand.CommandType
import com.mr.mf_pd.application.model.CheckParamsBean
import com.mr.mf_pd.application.model.Event
import com.mr.mf_pd.application.model.FileBeanModel
import com.mr.mf_pd.application.model.SettingBean
import com.mr.mf_pd.application.repository.callback.ReadSettingCallback
import com.mr.mf_pd.application.repository.impl.DataRepository
import com.mr.mf_pd.application.repository.impl.FilesRepository
import java.io.File

class FileDataViewModel(
    var dataRepository: DataRepository,
    val fileRepository: FilesRepository,
) : ViewModel() {

    var toastStr: MutableLiveData<String> = MutableLiveData()
    var settingBean: SettingBean? = null
    var checkParamsBean: MutableLiveData<CheckParamsBean>? = null
    lateinit var mCheckType: CheckType

    private var isStartReadYcData = false

    fun start(checkType: CheckType, file: File) {
        mCheckType = checkType
        dataRepository.setCheckType(checkType)

        CheckFileReadManager.get().addCallBack(CommandType.ReadYcData,ycCallback)
        CheckFileReadManager.get().addCallBack(CommandType.FdData,fdCallback)

        fileRepository.openCheckFile(checkType, file, object : ReadSettingCallback {
            override fun onGetFileBean(fileBean: FileBeanModel) {
                mCheckType.settingBean = fileBean.settingBean
                mCheckType.settingBean = fileBean.settingBean
                val checkDir = file.parentFile
                if (checkDir != null) {
                    fileRepository.setCurrentClickFile(checkDir)
                }
                checkParamsBean = checkType.checkParams
                CheckFileReadManager.get().config = fileBean.config
                CheckFileReadManager.get().startReadData()
            }
        })
    }

    private val _toYcDataEvent = MutableLiveData<Event<ByteArray>>()
    val toYcDataEvent: LiveData<Event<ByteArray>> = _toYcDataEvent

    private val _toFdDataEvent = MutableLiveData<Event<ByteArray>>()
    val toFdDataEvent: LiveData<Event<ByteArray>> = _toFdDataEvent

    private val ycCallback = object : BytesDataCallback {
        override fun onData(source: ByteArray) {
            if (source.isNotEmpty()) {
                _toYcDataEvent.postValue(Event(source))
            }
            if (!isStartReadYcData) {
                isStartReadYcData = true
            }
        }
    }

    private val fdCallback = object : BytesDataCallback {
        override fun onData(source: ByteArray) {
            if (source.isNotEmpty()) {
                _toFdDataEvent.postValue(Event(source))
            } else {
                _toFdDataEvent.postValue(Event(source))
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        CheckFileReadManager.get().removeCallBack(CommandType.ReadYcData,ycCallback)
        CheckFileReadManager.get().removeCallBack(CommandType.FdData,fdCallback)
        fileRepository.releaseReadFile()
    }
}