package com.mr.mf_pd.application.view.setting.aa

import android.text.TextUtils
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mr.mf_pd.application.common.CheckType
import com.mr.mf_pd.application.common.Constants
import com.mr.mf_pd.application.manager.socket.SocketManager
import com.mr.mf_pd.application.manager.socket.callback.ReadSettingDataCallback
import com.mr.mf_pd.application.manager.socket.comand.CommandHelp
import com.mr.mf_pd.application.manager.socket.comand.CommandType
import com.mr.mf_pd.application.repository.DefaultDataRepository
import com.mr.mf_pd.application.repository.impl.SettingRepository
import com.mr.mf_pd.application.utils.ByteUtil
import com.sito.tool.library.utils.ByteLibUtil
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.text.DecimalFormat

class AASettingViewModel(val setting: SettingRepository) : ViewModel() {


    lateinit var checkType: CheckType

    //实时上传
    var isUploadReal: MutableLiveData<Boolean> = MutableLiveData(true)

    var toastStr: MutableLiveData<String> = MutableLiveData()

    //警戒门限
    var jjLimitValueStr: MutableLiveData<String> = MutableLiveData()
    //频带上限
    var pdUpStr: MutableLiveData<String> = MutableLiveData("30")
    //频带下限
    var pdDownStr: MutableLiveData<String> = MutableLiveData("30")
    //幅值单位
    var fzUnitStr: MutableLiveData<String> = MutableLiveData("mV")

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
    //通道门限值调整步长
    var limitStepValueStr: MutableLiveData<String> = MutableLiveData()
    //触发门限值
    var cfLimitValueStr: MutableLiveData<String> = MutableLiveData()
    //低通滤波器
    var lowPassFilteringStr: MutableLiveData<String> = MutableLiveData()
    //高通滤波器
    var highPassFilteringStr: MutableLiveData<String> = MutableLiveData()

    //相位同步
    var phaseModelStr: MutableLiveData<String> = MutableLiveData()
    var phaseModelInt: MutableLiveData<Int> = MutableLiveData()
    //内同步的同步频率
    var phaseValueStr: MutableLiveData<String> = MutableLiveData()
    //频带检测
    var bandDetectionStr: MutableLiveData<String> = MutableLiveData()
    var bandDetectionInt: MutableLiveData<Int> = MutableLiveData()

    //自动同步
    var isAutoSync: MutableLiveData<Boolean> = MutableLiveData(true)

    //噪音过滤
    var isNoiseFiltering: MutableLiveData<Boolean> = MutableLiveData(true)

    //以放电量为单位
    var isFdUnit: MutableLiveData<Boolean> = MutableLiveData(true)

    //固定尺度
    var isFixedScale: MutableLiveData<Boolean> = MutableLiveData(false)

    //内同步频率
    var internalSyncStr: MutableLiveData<String> = MutableLiveData()

    //相位偏移
    var phaseOffsetStr: MutableLiveData<String> = MutableLiveData()

    //累计时间
    var totalTimeStr: MutableLiveData<String> = MutableLiveData()

    //校准系数，pc/mv
    var jzXsStr: MutableLiveData<String> = MutableLiveData()

    //校准器输出，pc
    var jzQShuChu: MutableLiveData<String> = MutableLiveData()

    //最大幅值
    var maximumAmplitudeStr1: MutableLiveData<String> = MutableLiveData()

    //最小幅值
    var minimumAmplitudeStr1: MutableLiveData<String> = MutableLiveData()

    //声音输出
    var isOutVoice: MutableLiveData<Boolean> = MutableLiveData(false)

    //自动增益
    var isAutoZy: MutableLiveData<Boolean> = MutableLiveData(false)

    //最大幅值2
    var maximumAmplitudeStr2: MutableLiveData<String> = MutableLiveData("-30")

    //最小幅值2
    var minimumAmplitudeStr2: MutableLiveData<String> = MutableLiveData("-80")

    fun start(checkType: CheckType) {
        this.checkType = checkType
        val settingBean = checkType.settingBean
        phaseModelInt.postValue(settingBean.xwTb)
        if (settingBean.xwTb<Constants.PHASE_MODEL_LIST.size){
            phaseModelStr.postValue(Constants.PHASE_MODEL_LIST[settingBean.xwTb])
        }
        isAutoSync.postValue(settingBean.autoTb == 1)
        isNoiseFiltering.postValue(settingBean.lyXc == 1)
        isFixedScale.postValue(settingBean.gdCd == 1)

        phaseOffsetStr.postValue(settingBean.xwPy.toString())
        totalTimeStr.postValue(settingBean.ljTime.toString())
        maximumAmplitudeStr1.postValue(settingBean.maxValue.toString())
        minimumAmplitudeStr1.postValue(settingBean.minValue.toString())

        isFdUnit.postValue(settingBean.fdlUnit == 1)
        jzXsStr.postValue(settingBean.jzRatio.toString())
        jzQShuChu.postValue(settingBean.jzOutValue.toString())
        fzUnitStr.postValue(settingBean.fzUnit)
        limitStepValueStr.postValue(settingBean.limitStepValue.toString())
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
        if (settingBean.cfLimitValue != null) {
            cfLimitValueStr.postValue(settingBean.cfLimitValue.toString())
        }
        if (settingBean.lowPassFiltering != null) {
            lowPassFilteringStr.postValue(settingBean.lowPassFiltering.toString())
        }
        if (settingBean.highPassFiltering != null) {
            highPassFilteringStr.postValue(settingBean.highPassFiltering.toString())
        }
        if (settingBean.phaseValue != null) {
            val df1 = DecimalFormat("0.00")
            phaseValueStr.postValue(df1.format(settingBean.phaseValue))
        }
        val fzUnit = settingBean.fzUnit
        if (!TextUtils.isEmpty(fzUnit)){
            fzUnitStr.postValue(fzUnit)
        }else{
            fzUnitStr.postValue(checkType.defaultUnit)
        }
        SocketManager.get().addCallBack(CommandType.ReadSettingValue,readSettingDataCallback)
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
            cfLimitValueStr.postValue(valueList[8].toInt().toString())
            lowPassFilteringStr.postValue(valueList[9].toInt().toString())
            highPassFilteringStr.postValue(valueList[10].toInt().toString())
            bandDetectionInt.postValue(valueList[11].toInt())
            bandDetectionStr.postValue(Constants.BAND_DETECTION_LIST[valueList[11].toInt()])
            phaseValueStr.postValue(valueList[12].toString())
            val df1 = DecimalFormat("0.00")
            phaseValueStr.postValue(df1.format(valueList[12]))
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
                val f = ByteLibUtil.byteArrayToFloat(value)
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
            settingBean.lyXc = if (isNoiseFiltering.value!!) 1 else 0
            settingBean.gdCd = if (isFixedScale.value!!) 1 else 0

            settingBean.xwPy = phaseOffsetStr.value!!.toInt()
            settingBean.ljTime = totalTimeStr.value!!.toInt()
            settingBean.maxValue = maximumAmplitudeStr1.value!!.toInt()
            settingBean.minValue = minimumAmplitudeStr1.value!!.toInt()
            settingBean.maxValue2= maximumAmplitudeStr2.value!!.toInt()
            settingBean.minValue2 = minimumAmplitudeStr2.value!!.toInt()
            settingBean.limitValue = limitValueStr.value?.toInt()
           limitStepValueStr.value?.toIntOrNull()?.let {
               settingBean.limitStepValue = it
           }
            settingBean.jjLimitValue = jjLimitValueStr.value?.toInt()
            settingBean.overLimitValue = overLimitValueStr.value?.toInt()
            settingBean.alarmLimitValue = alarmLimitValueStr.value?.toInt()
            settingBean.maxAverageValue = maxAverageValueStr.value?.toInt()
            settingBean.secondCycleMinValue = secondCycleMinValueStr.value?.toInt()
            settingBean.secondDischargeMinCount = secondDischargeMinCountStr.value?.toInt()
            settingBean.noiseLimit = noiseLimitStr.value?.toInt()

            if (cfLimitValueStr.value != null) {
                settingBean.cfLimitValue = cfLimitValueStr.value?.toInt()
            }
            if (lowPassFilteringStr.value != null) {
                settingBean.lowPassFiltering = lowPassFilteringStr.value?.toFloat()
            }
            if (highPassFilteringStr.value != null) {
                settingBean.highPassFiltering = highPassFilteringStr.value?.toFloat()
            }
            settingBean.phaseValue = phaseValueStr.value?.toFloatOrNull()
            val fzUnit = fzUnitStr.value
            if (!TextUtils.isEmpty(fzUnit)){
                settingBean.fzUnit = fzUnit!!
            }else{
                settingBean.fzUnit = checkType.defaultUnit
            }
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
        saveDataToList(cfLimitValueStr.value?.toFloatOrNull())
        saveDataToList(lowPassFilteringStr.value?.toFloatOrNull())
        saveDataToList(highPassFilteringStr.value?.toFloatOrNull())
        saveDataToList(phaseModelInt.value?.toFloat())
        saveDataToList(phaseValueStr.value?.toFloatOrNull())
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
        Log.d("zhangan","values ${values.toArray()}")
        val writeCommand = CommandHelp.writeSettingValue(checkType.passageway, values)
        SocketManager.get().sendData(writeCommand)
    }

    override fun onCleared() {
        super.onCleared()
        SocketManager.get().removeCallBack(CommandType.ReadSettingValue,readSettingDataCallback)
    }

}