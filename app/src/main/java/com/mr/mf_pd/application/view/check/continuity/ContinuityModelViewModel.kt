package com.mr.mf_pd.application.view.check.continuity

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mr.mf_pd.application.common.CheckType
import com.mr.mf_pd.application.manager.file.CheckFileReadManager
import com.mr.mf_pd.application.manager.socket.SocketManager
import com.mr.mf_pd.application.manager.socket.callback.BytesDataCallback
import com.mr.mf_pd.application.manager.socket.comand.CommandType
import com.mr.mf_pd.application.model.SettingBean
import com.mr.mf_pd.application.repository.impl.DataRepository
import com.mr.mf_pd.application.repository.impl.FilesRepository
import java.io.File

class ContinuityModelViewModel(
    val dataRepository: DataRepository,
    val filesRepository: FilesRepository,
) : ViewModel() {

    var toastStr: MutableLiveData<String> = MutableLiveData()
    var location: MutableLiveData<String> = MutableLiveData(filesRepository.getCurrentCheckName())
    var isSaveData: MutableLiveData<Boolean>? = null

    var isFile: MutableLiveData<Boolean> = MutableLiveData(false)

    var fzValueList: ArrayList<Float> = ArrayList()
    var yxValueList: ArrayList<Float> = ArrayList()
    var f1ValueList: ArrayList<Float> = ArrayList()
    var f2ValueList: ArrayList<Float> = ArrayList()

    var fzMinValue: MutableLiveData<String> = MutableLiveData()
    var fzValue: MutableLiveData<String> = MutableLiveData()
    var yxMinValue: MutableLiveData<String> = MutableLiveData()
    var yxValue: MutableLiveData<String> = MutableLiveData()
    var f1MinValue: MutableLiveData<String> = MutableLiveData()
    var f1Value: MutableLiveData<String> = MutableLiveData()
    var f2MinValue: MutableLiveData<String> = MutableLiveData()
    var f2Value: MutableLiveData<String> = MutableLiveData()

    var text1: MutableLiveData<String> = MutableLiveData()
    var text2: MutableLiveData<String> = MutableLiveData()
    var text3: MutableLiveData<String> = MutableLiveData()
    var text4: MutableLiveData<String> = MutableLiveData()

    lateinit var checkType: CheckType

    fun start() {
        if (isFile.value!!) {
            this.checkType = filesRepository.getCheckType()
        } else {
            this.isSaveData = filesRepository.isSaveData()
            this.checkType = dataRepository.getCheckType()
        }
        updateTitle(checkType.settingBean)
    }

    fun updateTitle(settingBean: SettingBean) {
        text1.postValue("有效值，" + settingBean.fzUnit)
        text2.postValue("峰值，" + settingBean.fzUnit)
        text3.postValue("F1(50Hz)，" + settingBean.fzUnit)
        text4.postValue("F2(100Hz)，" + settingBean.fzUnit)
    }

    private val ycBytesDataCallback = object : BytesDataCallback {
        override fun onData(source: ByteArray) {
            if (filesRepository.isSaveData()?.value == true) {
                filesRepository.toSaveYCData2File(source)
            }
        }
    }

    private val realBytesDataCallback = object : BytesDataCallback {
        override fun onData(source: ByteArray) {
            if (filesRepository.isSaveData()?.value == true) {
                filesRepository.toSaveRealData2File(source)
            }
        }
    }

    fun setCheckFile(filePath: String) {
        val file = File(filePath)
        filesRepository.setCurrentClickFile(file)
        location.postValue(filesRepository.getCurrentCheckName())
        createACheckFile()
    }

    fun createACheckFile() {
        filesRepository.toCreateCheckFile(checkType)
    }

    fun startSaveData() {
        filesRepository.startSaveData()
    }

    fun stopSaveData() {
        filesRepository.stopSaveData()
    }

    fun cleanCurrentData() {
        fzValueList.clear()
        yxValueList.clear()
        f1ValueList.clear()
        f2ValueList.clear()
    }

    fun onResume() {
        if (isFile.value!!) {
            CheckFileReadManager.get().addCallBack(CommandType.ReadYcData, ycBytesDataCallback)
            CheckFileReadManager.get().addCallBack(CommandType.RealData, realBytesDataCallback)
        } else {
            SocketManager.get().addCallBack(CommandType.ReadYcData, ycBytesDataCallback)
            SocketManager.get().addCallBack(CommandType.RealData, realBytesDataCallback)
        }
    }

    fun onPause() {
        if (isFile.value!!) {
            CheckFileReadManager.get().removeCallBack(CommandType.ReadYcData, ycBytesDataCallback)
            CheckFileReadManager.get().removeCallBack(CommandType.RealData, realBytesDataCallback)
        } else {
            SocketManager.get().removeCallBack(CommandType.ReadYcData, ycBytesDataCallback)
            SocketManager.get().removeCallBack(CommandType.RealData, realBytesDataCallback)
        }
    }

}