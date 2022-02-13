package com.mr.mf_pd.application.view.check.uhf.setting

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.common.primitives.Bytes
import com.mr.mf_pd.application.common.CheckType
import com.mr.mf_pd.application.common.Constants
import com.mr.mf_pd.application.manager.socket.CommandHelp
import com.mr.mf_pd.application.manager.socket.CommandType
import com.mr.mf_pd.application.manager.socket.SocketManager
import com.mr.mf_pd.application.repository.impl.SettingRepository
import com.mr.mf_pd.application.utils.ByteUtil
import org.checkerframework.checker.index.qual.LengthOf
import kotlin.math.max

class UHFSettingViewModel(val setting: SettingRepository) : ViewModel() {

    var toastStr: MutableLiveData<String> = MutableLiveData()

    lateinit var checkType: CheckType

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
        val command = CommandHelp.readSettingValue(checkType.type, 8)
        SocketManager.getInstance().sendData(command, CommandType.ReadSettingValue) {
            Log.d("zhangan", it.size.toString())
            val valueList = ArrayList<Float>()
            if (it.size > 2) {
                val length = it[2].toInt()
                val source = ByteArray(length * 4)

                System.arraycopy(it, 3, source, 0, it.size - 5)
                for (i in 0 until (source.size / 4)) {
                    val value = ByteArray(4)
                    System.arraycopy(source, 4 * i, value, 0, 4)
                    val f = ByteUtil.getFloat(value)
                    valueList.add(f)
                }
                Log.d("zhangan",valueList.toString())
            }
        }
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
    }

    fun toSave() {
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
        setting.toSaveSettingData(checkType)
    }

}