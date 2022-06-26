package com.mr.mf_pd.application.view.check

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mr.mf_pd.application.common.CheckType
import com.mr.mf_pd.application.common.Constants
import com.mr.mf_pd.application.manager.socket.SocketManager
import com.mr.mf_pd.application.manager.socket.callback.BytesDataCallback
import com.mr.mf_pd.application.manager.socket.callback.ReadSettingDataCallback
import com.mr.mf_pd.application.manager.socket.comand.CommandHelp
import com.mr.mf_pd.application.manager.socket.comand.CommandType
import com.mr.mf_pd.application.model.CheckParamsBean
import com.mr.mf_pd.application.model.Event
import com.mr.mf_pd.application.model.SettingBean
import com.mr.mf_pd.application.repository.impl.DataRepository
import com.mr.mf_pd.application.repository.impl.SettingRepository
import com.mr.mf_pd.application.opengl.`object`.FlightPoint2DList
import com.mr.mf_pd.application.opengl.`object`.PrPsCubeList
import com.mr.mf_pd.application.opengl.`object`.PrPdPoint2DList
import com.mr.mf_pd.application.opengl.`object`.PrpsPointList
import com.sito.tool.library.utils.ByteLibUtil
import io.reactivex.disposables.Disposable

class CheckDataViewModel(
    private val dataRepository: DataRepository,
    private val settingRepository: SettingRepository,
) : ViewModel() {
    lateinit var mCheckType: CheckType
    var writeSetting = false
    var toastStr: MutableLiveData<String> = MutableLiveData()
    var settingBean: SettingBean? = null
    var checkParamsBean: MutableLiveData<CheckParamsBean>? = null
    val settingValues: ArrayList<Float> = ArrayList()

    var writeSettingCommand: ByteArray? = null

    private val _toYcDataEvent = MutableLiveData<Event<ByteArray>>()
    val toYcDataEvent: LiveData<Event<ByteArray>> = _toYcDataEvent

    private val _toSettingValueEvent = MutableLiveData<Event<Unit>>()
    val toSettingValueEvent: LiveData<Event<Unit>> = _toSettingValueEvent

    private val disposableList = ArrayList<Disposable>()

    fun start(checkType: CheckType) {
        mCheckType = checkType
        settingBean = settingRepository.getSettingData(checkType)
        dataRepository.setCheckType(checkType)
        checkParamsBean = checkType.checkParams
        updateSettingValue()
        SocketManager.get().addCallBack(CommandType.ReadSettingValue, readSettingDataCallback)
        SocketManager.get().addCallBack(CommandType.WriteValue, writeSettingDataCallback)
        SocketManager.get().addCallBack(CommandType.ReadYcData, ycBytesDataCallback)
        readYcValue()
    }

    private fun updateSettingValue() {
        settingBean?.let {
            PrPdPoint2DList.maxValue = it.maxValue.toFloat()
            PrPdPoint2DList.minValue = it.minValue.toFloat()

            PrpsPointList.maxValue = it.maxValue.toFloat()
            PrpsPointList.minValue = it.minValue.toFloat()

            PrPsCubeList.maxValue = it.maxValue.toFloat()
            PrPsCubeList.minValue = it.minValue.toFloat()

            FlightPoint2DList.maxValue = it.maxValue.toFloat()
            FlightPoint2DList.minValue = it.minValue.toFloat()
        }
    }

    private fun readYcValue() {
        if (mCheckType == CheckType.HF || mCheckType == CheckType.UHF) {
            disposableList.add(dataRepository.readRepeatData())
        } else {
            disposableList.add(dataRepository.readContinuityYcData())
        }
    }

    fun updateCallback() {
        val command = CommandHelp.readSettingValue(mCheckType.passageway, mCheckType.settingLength)
        disposableList.add(SocketManager.get().sendData(command))
    }

    private val readSettingDataCallback = object : ReadSettingDataCallback {
        override fun onData(source: ByteArray) {
            dealSettingValue(source)
        }
    }

    private val writeSettingDataCallback = object : BytesDataCallback {

        override fun onData(source: ByteArray) {
            writeSetting = false
            if (source.contentEquals(writeSettingCommand)) {
                dealWriteSettingValue(source)
            }
        }
    }

    private fun dealSettingValue(bytes: ByteArray) {
        val valueList = splitBytesToValue(bytes)
        if (valueList.size == mCheckType.settingLength) {
            settingValues.clear()
            settingValues.addAll(valueList)
            when (mCheckType) {
                CheckType.AE -> {
                    settingBean?.limitValue = valueList[7].toInt()
                    checkParamsBean?.value?.phaseAttr =
                        Constants.PHASE_MODEL_LIST[valueList[11].toInt()]
                }
                CheckType.AA -> {
                    settingBean?.limitValue = valueList[7].toInt()
                    checkParamsBean?.value?.phaseAttr =
                        Constants.PHASE_MODEL_LIST[valueList[11].toInt()]
                }
                CheckType.TEV -> {
                    settingBean?.limitValue = valueList[7].toInt()
                    checkParamsBean?.value?.frequencyBandAttr =
                        Constants.BAND_DETECTION_LIST[valueList[8].toInt()]
                }
                CheckType.HF -> {
                    settingBean?.limitValue = valueList[7].toInt()
                    val position = valueList[11].toInt()
                    if (position < Constants.PHASE_MODEL_LIST.size) {
                        checkParamsBean?.value?.phaseAttr =
                            Constants.PHASE_MODEL_LIST[valueList[11].toInt()]
                    }
                }
                CheckType.UHF -> {
                    settingBean?.limitValue = valueList[7].toInt()
                    val bandIndex = valueList[8].toInt()
                    checkParamsBean?.value?.frequencyBandAttr =
                        Constants.BAND_DETECTION_LIST[bandIndex]
                    val phaseIndex = valueList[9].toInt()
                    if (phaseIndex < Constants.PHASE_MODEL_LIST.size) {
                        checkParamsBean?.value?.phaseAttr =
                            Constants.PHASE_MODEL_LIST[valueList[9].toInt()]
                    }
                }
            }
            checkParamsBean?.postValue(checkParamsBean?.value)
        }
        updateSettingValue()
        _toSettingValueEvent.postValue(Event(Unit))
    }


    private fun dealWriteSettingValue(bytes: ByteArray) {
        val valueList = splitWriteBytesToValue(bytes)
        if (valueList.size == mCheckType.settingLength) {
            settingValues.clear()
            settingValues.addAll(valueList)
            when (mCheckType) {
                CheckType.AE -> {
                    settingBean?.limitValue = valueList[7].toInt()
                    checkParamsBean?.value?.phaseAttr =
                        Constants.PHASE_MODEL_LIST[valueList[11].toInt()]
                }
                CheckType.AA -> {
                    settingBean?.limitValue = valueList[7].toInt()
                    checkParamsBean?.value?.phaseAttr =
                        Constants.PHASE_MODEL_LIST[valueList[11].toInt()]
                }
                CheckType.TEV -> {
                    settingBean?.limitValue = valueList[7].toInt()
                    checkParamsBean?.value?.frequencyBandAttr =
                        Constants.BAND_DETECTION_LIST[valueList[8].toInt()]
                    toastStr.postValue(Constants.BAND_DETECTION_LIST[valueList[8].toInt()])
                }
                CheckType.HF -> {
                    settingBean?.limitValue = valueList[7].toInt()
                    val position = valueList[11].toInt()
                    if (position < Constants.PHASE_MODEL_LIST.size) {
                        checkParamsBean?.value?.phaseAttr =
                            Constants.PHASE_MODEL_LIST[valueList[11].toInt()]
                    }
                }
                CheckType.UHF -> {
                    settingBean?.limitValue = valueList[7].toInt()
                    checkParamsBean?.value?.frequencyBandAttr =
                        Constants.BAND_DETECTION_LIST[valueList[8].toInt()]
                    checkParamsBean?.value?.phaseAttr =
                        Constants.PHASE_MODEL_LIST[valueList[9].toInt()]
                    toastStr.postValue(Constants.BAND_DETECTION_LIST[valueList[8].toInt()])
                }
            }
            checkParamsBean?.postValue(checkParamsBean?.value)
        }
        updateSettingValue()
    }


    private fun splitBytesToValue(bytes: ByteArray): ArrayList<Float> {
        val valueList = ArrayList<Float>()
        if (bytes.size > 2) {
            val length = bytes[2].toInt()
            val source = ByteArray(length * 4)
            System.arraycopy(bytes, 3, source, 0, bytes.size - 5)
            for (i in 0 until (source.size / 4)) {
                val value = ByteArray(4)
                System.arraycopy(source, 4 * i, value, 0, 4)
                val f = ByteLibUtil.byteArrayToFloat(value)
                valueList.add(f)
            }
        }
        return valueList
    }

    private fun splitWriteBytesToValue(bytes: ByteArray): ArrayList<Float> {
        val valueList = ArrayList<Float>()
        if (bytes.size > 2) {
            val length = bytes[2].toInt() - 1
            val source = ByteArray(length)
            System.arraycopy(bytes, 4, source, 0, bytes.size - 6)
            for (i in 0 until (source.size / 4)) {
                val value = ByteArray(4)
                System.arraycopy(source, 4 * i, value, 0, 4)
                val f = ByteLibUtil.byteArrayToFloat(value)
                valueList.add(f)
            }
        }
        return valueList
    }

    fun writeValue() {
        if (settingValues.isNotEmpty()) {
            writeSettingCommand =
                CommandHelp.writeSettingValue(mCheckType.passageway, settingValues)
            SocketManager.get().sendData(writeSettingCommand)
        }
    }

    private val ycBytesDataCallback = object : BytesDataCallback {
        override fun onData(source: ByteArray) {
            _toYcDataEvent.postValue(Event(source))
        }
    }

    override fun onCleared() {
        super.onCleared()
        SocketManager.get().removeCallBack(CommandType.ReadSettingValue, readSettingDataCallback)
        SocketManager.get().removeCallBack(CommandType.WriteValue, writeSettingDataCallback)
        SocketManager.get().removeCallBack(CommandType.ReadYcData, ycBytesDataCallback)
        dataRepository.closePassageway()
        disposableList.forEach { disposable ->
            if (!disposable.isDisposed) {
                disposable.dispose()
            }
        }
        disposableList.clear()
    }
}