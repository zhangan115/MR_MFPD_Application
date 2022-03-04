package com.mr.mf_pd.application.view.check.uhf.setting

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mr.mf_pd.application.common.CheckType
import com.mr.mf_pd.application.common.Constants
import com.mr.mf_pd.application.manager.socket.comand.CommandHelp
import com.mr.mf_pd.application.manager.socket.comand.CommandType
import com.mr.mf_pd.application.manager.socket.SocketManager
import com.mr.mf_pd.application.manager.socket.callback.ReadSettingDataCallback
import com.mr.mf_pd.application.repository.DefaultDataRepository
import com.mr.mf_pd.application.repository.impl.SettingRepository
import com.mr.mf_pd.application.utils.ByteUtil
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class UHFSettingViewModel(val setting: SettingRepository) : ViewModel() {

    var toastStr: MutableLiveData<String> = MutableLiveData()

    lateinit var checkType: CheckType

    //警戒门限
    var jjLimitValueStr: MutableLiveData<String> = MutableLiveData()

    //过高门限
    var overLimitValueStr: MutableLiveData<String> = MutableLiveData()

    //告警门限
    var alarmLimitValueStr: MutableLiveData<String> = MutableLiveData()

    //最大幅值与平均值最小差值
    var maxAverageValueStr: MutableLiveData<String> = MutableLiveData()

    //1秒放电周期最小值
    var secondCycleMinValueStr: MutableLiveData<String> = MutableLiveData()

    //1秒最小放电次数
    var secondDischargeMinCountStr: MutableLiveData<String> = MutableLiveData()

    //噪声宽度门限
    var noiseLimitStr: MutableLiveData<String> = MutableLiveData()

    //通道门限值
    var limitValueStr: MutableLiveData<String> = MutableLiveData()

    //实时上传
    var isUploadReal: MutableLiveData<Boolean> = MutableLiveData(true)

    //相位同步
    var phaseModelStr: MutableLiveData<String> = MutableLiveData()
    var phaseModelInt: MutableLiveData<Int> = MutableLiveData()

    //频带检测
    var bandDetectionStr: MutableLiveData<String> = MutableLiveData()
    var bandDetectionInt: MutableLiveData<Int> = MutableLiveData()

    //自动同步
    var isAutoSync: MutableLiveData<Boolean> = MutableLiveData(true)

    //噪音过滤
    var isNoiseFiltering: MutableLiveData<Boolean> = MutableLiveData(true)

    //固定尺度
    var isFixedScale: MutableLiveData<Boolean> = MutableLiveData(false)

    //内同步频率
    var internalSyncStr: MutableLiveData<String> = MutableLiveData()

    //相位偏移
    var phaseOffsetStr: MutableLiveData<String> = MutableLiveData()

    //累计时间
    var totalTimeStr: MutableLiveData<String> = MutableLiveData()

    //最大幅值
    var maximumAmplitudeStr: MutableLiveData<String> = MutableLiveData()

    //最小幅值
    var minimumAmplitudeStr: MutableLiveData<String> = MutableLiveData()

    fun start(checkType: CheckType) {
        this.checkType = checkType
        val settingBean = checkType.settingBean
        phaseModelInt.postValue(settingBean.xwTb)
        phaseModelStr.postValue(Constants.PHASE_MODEL_LIST[settingBean.xwTb])
        bandDetectionInt.postValue(settingBean.pdJc)
        bandDetectionStr.postValue(Constants.BAND_DETECTION_LIST[settingBean.pdJc])
        isAutoSync.postValue(settingBean.autoTb == 1)
        isNoiseFiltering.postValue(settingBean.lyXc == 1)
        isFixedScale.postValue(settingBean.gdCd == 1)
        internalSyncStr.postValue(settingBean.nTbPl.toString())
        phaseOffsetStr.postValue(settingBean.xwPy.toString())
        totalTimeStr.postValue(settingBean.ljTime.toString())
        maximumAmplitudeStr.postValue(settingBean.maxValue.toString())
        minimumAmplitudeStr.postValue(settingBean.minValue.toString())
        if (settingBean.limitValue != null) {
            limitValueStr.postValue(settingBean.limitValue.toString())
        }
        if (settingBean.jjLimitValue != null) {
            jjLimitValueStr.postValue(settingBean.jjLimitValue.toString())
        }
        if (settingBean.overLimitValue != null) {
            overLimitValueStr.postValue(settingBean.overLimitValue.toString())
        }
        if (settingBean.alarmLimitValue!=null) {
            alarmLimitValueStr.postValue(settingBean.alarmLimitValue.toString())
        }
        if (settingBean.maxAverageValue != null) {
            maxAverageValueStr.postValue(settingBean.maxAverageValue.toString())
        }
        if (settingBean.secondCycleMinValue != null) {
            secondCycleMinValueStr.postValue(settingBean.secondCycleMinValue.toString())
        }
        if (settingBean.secondDischargeMinCount != null) {
            secondDischargeMinCountStr.postValue(settingBean.secondDischargeMinCount.toString())
        }
        if (settingBean.noiseLimit != null) {
            noiseLimitStr.postValue(settingBean.noiseLimit.toString())
        }
        SocketManager.get().addReadSettingCallback(readSettingDataCallback)
        val readSettingCommand = CommandHelp.readSettingValue(checkType.passageway, checkType.settingLength)
        SocketManager.get()
            .sendData(readSettingCommand)
    }

    private val readSettingDataCallback = object : ReadSettingDataCallback {
        override fun onData(source: ByteArray) {
            dealSettingValue(source)
        }
    }

    private fun dealSettingValue(bytes: ByteArray) {
        val valueList = splitBytesToValue(bytes)
        if (valueList.size >= checkType.settingLength) {
            jjLimitValueStr.postValue(valueList[0].toInt().toString())
            overLimitValueStr.postValue(valueList[1].toInt().toString())
            alarmLimitValueStr.postValue(valueList[2].toInt().toString())
            maxAverageValueStr.postValue(valueList[3].toInt().toString())
            secondCycleMinValueStr.postValue(valueList[4].toInt().toString())
            secondDischargeMinCountStr.postValue(valueList[5].toInt().toString())
            noiseLimitStr.postValue(valueList[6].toInt().toString())
            limitValueStr.postValue(valueList[7].toInt().toString())
            bandDetectionInt.postValue(valueList[8].toInt())
            bandDetectionStr.postValue(Constants.BAND_DETECTION_LIST[valueList[8].toInt()])
            phaseModelInt.postValue(valueList[9].toInt())
            phaseModelStr.postValue(Constants.PHASE_MODEL_LIST[valueList[9].toInt()])
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

    fun toSave() {
        GlobalScope.launch {
            toWriteSettingValue()
            val settingBean = checkType.settingBean
            settingBean.xwTb = phaseModelInt.value!!
            settingBean.pdJc = bandDetectionInt.value!!
            settingBean.autoTb = if (isAutoSync.value!!) 1 else 0
            settingBean.lyXc = if (isNoiseFiltering.value!!) 1 else 0
            settingBean.gdCd = if (isFixedScale.value!!) 1 else 0
            settingBean.nTbPl = internalSyncStr.value!!.toFloat()
            settingBean.xwPy = phaseOffsetStr.value!!.toInt()
            settingBean.ljTime = totalTimeStr.value!!.toInt()
            settingBean.maxValue = maximumAmplitudeStr.value!!.toInt()
            settingBean.minValue = minimumAmplitudeStr.value!!.toInt()
            settingBean.limitValue = limitValueStr.value!!.toInt()
            settingBean.jjLimitValue = jjLimitValueStr.value!!.toInt()
            settingBean.overLimitValue = overLimitValueStr.value!!.toInt()
            settingBean.alarmLimitValue = alarmLimitValueStr.value!!.toInt()
            settingBean.maxAverageValue = maxAverageValueStr.value!!.toInt()
            settingBean.secondCycleMinValue = secondCycleMinValueStr.value!!.toInt()
            settingBean.secondDischargeMinCount = secondDischargeMinCountStr.value!!.toInt()
            settingBean.noiseLimit = noiseLimitStr.value!!.toInt()
            DefaultDataRepository.realDataMaxValue.postValue(settingBean.maxValue)
            DefaultDataRepository.realDataMinValue.postValue(settingBean.minValue)
            setting.toSaveSettingData(checkType)
        }
    }

    val values = ArrayList<Float>()

    private fun toWriteSettingValue() {
        values.clear()
        saveDataToList(jjLimitValueStr.value?.toFloatOrNull())
        saveDataToList(overLimitValueStr.value?.toFloatOrNull())
        saveDataToList(alarmLimitValueStr.value?.toFloatOrNull())
        saveDataToList(maxAverageValueStr.value?.toFloatOrNull())
        saveDataToList(secondCycleMinValueStr.value?.toFloatOrNull())
        saveDataToList(secondDischargeMinCountStr.value?.toFloatOrNull())
        saveDataToList(noiseLimitStr.value?.toFloatOrNull())
        saveDataToList(limitValueStr.value?.toFloatOrNull())
        saveDataToList(bandDetectionInt.value?.toFloat())
        saveDataToList(phaseModelInt.value?.toFloat())
        if (values.size == checkType.settingLength) {
            writeValue()
        }
    }

    private fun saveDataToList(float: Float?) {
        if (float == null) {
            values.add(0f)
        } else {
            values.add(float)
        }
    }

    private fun writeValue() {
        val writeCommand = CommandHelp.writeSettingValue(checkType.passageway, values)
        SocketManager.get().sendData(writeCommand)
    }

    override fun onCleared() {
        super.onCleared()
        SocketManager.get().removeReadSettingCallback(readSettingDataCallback)
    }

}