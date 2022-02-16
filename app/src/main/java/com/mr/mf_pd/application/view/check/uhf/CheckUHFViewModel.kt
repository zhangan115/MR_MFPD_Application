package com.mr.mf_pd.application.view.check.uhf

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mr.mf_pd.application.common.CheckType
import com.mr.mf_pd.application.common.Constants
import com.mr.mf_pd.application.manager.socket.CommandHelp
import com.mr.mf_pd.application.manager.socket.CommandType
import com.mr.mf_pd.application.manager.socket.SocketManager
import com.mr.mf_pd.application.model.CheckParamsBean
import com.mr.mf_pd.application.model.SettingBean
import com.mr.mf_pd.application.repository.impl.DataRepository
import com.mr.mf_pd.application.repository.impl.SettingRepository
import com.mr.mf_pd.application.utils.ByteUtil
import com.mr.mf_pd.application.view.opengl.`object`.PrPsCubeList
import com.mr.mf_pd.application.view.opengl.`object`.PrpsPoint2DList
import com.mr.mf_pd.application.view.opengl.`object`.PrpsPointList
import io.reactivex.disposables.Disposable

class CheckUHFViewModel(
    private val dataRepository: DataRepository,
    private val settingRepository: SettingRepository,
) : ViewModel() {

    var toastStr: MutableLiveData<String> = MutableLiveData()
    private val disposableList = ArrayList<Disposable>()
    var settingBean: SettingBean? = null
    var checkParamsBean: MutableLiveData<CheckParamsBean>? = null

    fun start(checkType: CheckType) {
        settingBean = settingRepository.getSettingData(checkType)
        settingBean?.let {
            PrpsPoint2DList.maxValue = it.maxValue.toFloat()
            PrpsPoint2DList.minValue = it.minValue.toFloat()

            PrpsPointList.maxValue = it.maxValue.toFloat()
            PrpsPointList.minValue = it.minValue.toFloat()

            PrPsCubeList.maxValue = it.maxValue.toFloat()
            PrPsCubeList.minValue = it.minValue.toFloat()
        }
        checkParamsBean = checkType.checkParams
        dataRepository.setCheckType(checkType)
        readUHFValue(checkType)
    }

    private fun readUHFValue(checkType: CheckType) {
        //读取设置
        val command = CommandHelp.readSettingValue(checkType.type, 10)
        disposableList.add(
            SocketManager.getInstance()
                .sendData(command, CommandType.ReadSettingValue) { settingBytes ->
                    dealSettingValue(settingBytes)
                    val readYcCommand = CommandHelp.readYcValue(checkType.type)
                    //读取遥测
                    disposableList.add(
                        SocketManager.getInstance()
                            .sendData(readYcCommand, CommandType.ReadYcData) { ycBytes ->
                                dealYcValue(ycBytes)
                                dataRepository.switchPassageway(checkType.type)
                            })
                })
    }

    private fun dealSettingValue(bytes: ByteArray) {
        val valueList = splitBytesToValue(bytes)
        if (valueList.size >= 10) {
            settingBean?.limitValue = valueList[7].toInt()
            checkParamsBean?.value?.frequencyBandAttr = Constants.BAND_DETECTION_LIST[valueList[8].toInt()]
            checkParamsBean?.value?.phaseAttr = Constants.PHASE_MODEL_LIST[valueList[9].toInt()]
            checkParamsBean?.postValue(checkParamsBean?.value)
        }
    }

    private fun dealYcValue(bytes: ByteArray) {
        val valueList = splitBytesToValue(bytes)
        if (valueList.size >= 2) {
            //频率
            checkParamsBean?.value?.hzAttr = valueList[1].toString()
            checkParamsBean?.postValue(checkParamsBean?.value)
        }
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

    override fun onCleared() {
        super.onCleared()
        dataRepository.closePassageway()
        disposableList.forEach { disposable ->
            if (!disposable.isDisposed) {
                disposable.dispose()
            }
        }
        disposableList.clear()
    }
}