package com.mr.mf_pd.application.view.check.pulse

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mr.mf_pd.application.common.CheckType
import com.mr.mf_pd.application.manager.file.CheckFileReadManager
import com.mr.mf_pd.application.manager.socket.SocketManager
import com.mr.mf_pd.application.manager.socket.callback.BytesDataCallback
import com.mr.mf_pd.application.manager.socket.comand.CommandType
import com.mr.mf_pd.application.model.Event
import com.mr.mf_pd.application.repository.impl.DataRepository
import com.mr.mf_pd.application.repository.impl.FilesRepository
import com.mr.mf_pd.application.utils.ByteUtil
import com.mr.mf_pd.application.utils.DateUtil
import com.mr.mf_pd.application.utils.RepeatActionUtils
import com.mr.mf_pd.application.view.callback.FdDataCallback
import io.reactivex.disposables.Disposable
import java.io.File
import java.util.*
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.math.max

class ACPulseModelViewModel(
    val dataRepository: DataRepository,
    val filesRepository: FilesRepository,
) : ViewModel() {

    var toastStr: MutableLiveData<String> = MutableLiveData()
    var fdStateStr: MutableLiveData<String> = MutableLiveData()
    var location: MutableLiveData<String> = MutableLiveData()

    var synchronizationModel: MutableLiveData<String> = MutableLiveData()
    var gainLevelStr: MutableLiveData<String> = MutableLiveData()
    var gainMinValue: MutableLiveData<Float?> = MutableLiveData()
    var showTimeView: MutableLiveData<Boolean> = MutableLiveData<Boolean>(false)
    lateinit var checkType: CheckType

    var gainValues: MutableLiveData<Vector<Float>> = MutableLiveData(Vector<Float>())
    var isFile: MutableLiveData<Boolean> = MutableLiveData(false)
    var limitValueStr: MutableLiveData<String> = MutableLiveData()

    var timeStr: MutableLiveData<String> = MutableLiveData()
    var mTimeDisposable: Disposable? = null

    private val _toResetEvent = MutableLiveData<Event<Unit>>()
    val toResetEvent: LiveData<Event<Unit>> = _toResetEvent

    fun start() {
        if (isFile.value!!) {
            this.checkType = filesRepository.getCheckType()
            val checkDataFileModel = filesRepository.getCheckFileModel()
            this.showTimeView.postValue(true)
            var startTime = 0L
            checkDataFileModel?.let {
                mTimeDisposable = RepeatActionUtils.execute {
                    it.dataTime?.let {
                        timeStr.postValue(DateUtil.timeFormat((it - startTime), "mm:ss"))
                        startTime += 1000L
                        if (startTime > it) {
                            resetFileRead()
                            startTime = 0
                        }
                    }
                }
            }
        } else {
            this.isSaveData = filesRepository.isSaveData()
            this.checkType = dataRepository.getCheckType()
            this.timeStr = filesRepository.getSaveDataTime()
            showTimeView.postValue(false)
        }
    }

    private fun resetFileRead() {
        _toResetEvent.postValue(Event(Unit))
        CheckFileReadManager.get().startReadData()
    }

    var isSaveData: MutableLiveData<Boolean>? = null

    fun startSaveData() {
        filesRepository.startSaveData()
    }

    fun stopSaveData() {
        filesRepository.stopSaveData()
    }

    fun getQueue(): ArrayBlockingQueue<ByteArray>? {
        return if (isFile.value!!) {
            CheckFileReadManager.get().pulseDataDeque
        } else {
            SocketManager.get().pulseDataDeque
        }
    }

    fun onResume() {
        if (isFile.value!!) {
            CheckFileReadManager.get().addCallBack(CommandType.SendPulse, pulseValueCallBack)
        } else {
            SocketManager.get().addCallBack(CommandType.SendPulse, pulseValueCallBack)
        }
    }

    fun onPause() {
        if (isFile.value!!) {
            CheckFileReadManager.get().removeCallBack(CommandType.SendPulse, pulseValueCallBack)
        } else {
            SocketManager.get().removeCallBack(CommandType.SendPulse, pulseValueCallBack)
        }
    }

    private var maxGainValue: Float? = null
    var receiverCount = 0

    private var fdCallback: FdDataCallback? = null

    fun setDataCallback(callback: FdDataCallback) {
        fdCallback = callback
    }

    fun cleanCurrentData() {
        receiverCount = 0
        val list = Vector<Float>()
        if (isFile.value == true) {
            this.gainValues.postValue(list)
        } else {
            this.gainValues.postValue(list)
        }
    }

    val pulseValueCallBack = object : BytesDataCallback {
        override fun onData(source: ByteArray) {
            val valueList = CopyOnWriteArrayList<Float?>()
            val onePulseCount = 11 + 249 * 2 + 10 + 2
            if (source.isEmpty() || source.size < onePulseCount) return
            val pulseCount = source[10].toInt()
            val bytes = ByteArray(249 * 2)
            val srcPos = 11 + (pulseCount - 1) * (249 * 2 + 10)
            System.arraycopy(source, srcPos + 10, bytes, 0, 249 * 2)
            if (bytes.isNotEmpty() && bytes.size % 2 == 0) {
                var position = 0
                while (position < bytes.size) {
                    val values = ByteArray(2)
                    values[0] = bytes[position]
                    values[1] = bytes[position + 1]
                    val value = ByteUtil.byte2short(values) * 0.1f
                    maxGainValue = if (maxGainValue == null) {
                        value
                    } else {
                        max(value, maxGainValue!!)
                    }
                    valueList.add(value)
                    position += 2
                }
            }
            if (receiverCount % 5 == 0) {
                if (maxGainValue != null) {
                    gainValues.value?.add(maxGainValue!!)
                }
                if (gainValues.value != null) {
                    if (gainValues.value!!.size > checkType.settingBean.ljTime * 10) {
                        gainValues.value?.removeFirstOrNull()
                    }
                }
                gainValues.postValue(gainValues.value)
                maxGainValue = null
            }
            receiverCount++
            fdCallback?.fdData(valueList)
        }

    }

    fun setState(stateStr: String?) {
        fdStateStr.value = stateStr
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

}