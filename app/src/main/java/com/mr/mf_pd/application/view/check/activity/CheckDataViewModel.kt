package com.mr.mf_pd.application.view.check.activity

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mr.mf_pd.application.common.CheckType
import com.mr.mf_pd.application.common.Constants
import com.mr.mf_pd.application.manager.socket.SocketManager
import com.mr.mf_pd.application.manager.socket.callback.BaseDataCallback
import com.mr.mf_pd.application.manager.socket.callback.ReadSettingDataCallback
import com.mr.mf_pd.application.manager.socket.callback.WriteSettingDataCallback
import com.mr.mf_pd.application.manager.socket.callback.YcDataCallback
import com.mr.mf_pd.application.manager.socket.comand.CommandHelp
import com.mr.mf_pd.application.model.CheckParamsBean
import com.mr.mf_pd.application.model.Event
import com.mr.mf_pd.application.model.SettingBean
import com.mr.mf_pd.application.repository.impl.DataRepository
import com.mr.mf_pd.application.repository.impl.SettingRepository
import com.mr.mf_pd.application.utils.ByteUtil
import com.mr.mf_pd.application.view.opengl.`object`.PrPsCubeList
import com.mr.mf_pd.application.view.opengl.`object`.PrpsPoint2DList
import com.mr.mf_pd.application.view.opengl.`object`.PrpsPointList
import io.reactivex.disposables.Disposable

class CheckDataViewModel(
    val dataRepository: DataRepository,
    val settingRepository: SettingRepository,
) : ViewModel() {
    var writeSetting = false
    var writeSettingCommand: ByteArray? = null
    var toastStr: MutableLiveData<String> = MutableLiveData()
    private val disposableList = ArrayList<Disposable>()
    var settingBean: SettingBean? = null
    var checkParamsBean: MutableLiveData<CheckParamsBean>? = null
    val settingValues: ArrayList<Float> = ArrayList()
    private var disposable: Disposable? = null
    lateinit var mCheckType: CheckType

    private val _toYcDataEvent = MutableLiveData<Event<ByteArray>>()
    val toYcDataEvent: LiveData<Event<ByteArray>> = _toYcDataEvent

    fun start(checkType: CheckType) {
        mCheckType = checkType
        dataRepository.setCheckType(checkType)
        updateSettingValue(checkType)
        checkParamsBean = checkType.checkParams
        openPassageway()
    }

    private fun openPassageway() {
        val command = CommandHelp.switchPassageway(mCheckType.passageway)
        dataRepository.switchPassageway(mCheckType.passageway)
        SocketManager.get().addWriteSettingCallback(writeSettingDataCallback)
        SocketManager.get().openPassageway = object : BaseDataCallback {
            override fun onData(source: ByteArray) {
                if (source.contentEquals(command)) {
                    readYcValue()
                }
            }
        }
    }

    private fun updateSettingValue(checkType: CheckType) {
        settingBean = settingRepository.getSettingData(checkType)
        settingBean?.let {
            PrpsPoint2DList.maxValue = it.maxValue.toFloat()
            PrpsPoint2DList.minValue = it.minValue.toFloat()

            PrpsPointList.maxValue = it.maxValue.toFloat()
            PrpsPointList.minValue = it.minValue.toFloat()

            PrPsCubeList.maxValue = it.maxValue.toFloat()
            PrPsCubeList.minValue = it.minValue.toFloat()
        }
    }

    private fun readYcValue() {
        dataRepository.addYcDataCallback(object : BaseDataCallback {
            override fun onData(source: ByteArray) {
                _toYcDataEvent.postValue(Event(source))
            }
        })
        if (disposable == null) {
            disposable = dataRepository.readRepeatData()
        }
        updateCallback()
    }

    fun updateCallback() {
        val command = CommandHelp.readSettingValue(mCheckType.passageway, mCheckType.settingLength)
        disposableList.add(SocketManager.get().sendData(command))
        SocketManager.get().addReadSettingCallback(readSettingDataCallback)
    }

    private val readSettingDataCallback = object : ReadSettingDataCallback {
        override fun onData(source: ByteArray) {
            dealSettingValue(source)
        }
    }

    private val writeSettingDataCallback = object : WriteSettingDataCallback {

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
                    checkParamsBean?.value?.frequencyBandAttr =
                        Constants.BAND_DETECTION_LIST[valueList[8].toInt()]
                    checkParamsBean?.value?.phaseAttr =
                        Constants.PHASE_MODEL_LIST[valueList[9].toInt()]
                }
            }
            checkParamsBean?.postValue(checkParamsBean?.value)
        }
        updateSettingValue(mCheckType)
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
        updateSettingValue(mCheckType)
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
                val f = ByteUtil.getFloat(value)
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
                val f = ByteUtil.getFloat(value)
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

    override fun onCleared() {
        super.onCleared()
        dataRepository.closePassageway()
        disposableList.forEach { disposable ->
            if (!disposable.isDisposed) {
                disposable.dispose()
            }
        }
        disposableList.clear()
        disposable?.dispose()
        disposable = null
        SocketManager.get().removeReadSettingCallback(readSettingDataCallback)
        SocketManager.get().openPassageway = null
    }
}