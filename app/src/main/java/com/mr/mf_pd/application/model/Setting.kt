package com.mr.mf_pd.application.model

import android.os.Parcel
import android.os.Parcelable

data class SettingBean(
    var xwTb: Int = 0,//相位同步
    var autoTb: Int = 0,//自动同步
    var pdJc: Int = 0,//频带检测
    var lyXc: Int = 1,//滤音消除
    var xwPy: Int = 0,//相位偏移
    var ljTime: Int = 15,//图谱累积时间
    var gdCd: Int = 1,//固定尺度
    var maxValue: Int = -20,//最大幅值
    var minValue: Int = -80,//最小幅值

    var nTbPl: Float = 0f,//内同步频率
    var fzUnit: String? = "",//幅值单位
    var outVoice: Int = 0, //声音输出
    var autoZy: Int = 0, //自定增益
    var pdDown: Int = 50, //频带下限
    var pdUp: Int = 300, //频带上限
    var maxValue2: Int = 0,//最大幅值2
    var minValue2: Int = 0,//最小幅值2

    var fdlUnit: Int = 0, //以放电量单位
    var jzRatio: Float = 1.0f,//校准系数
    var jzOutValue: Float = 10.0f,//校准器输出
    //通道门限值
    var limitValue: Int? = null,
    //警戒门限
    var jjLimitValue: Int? = null,
    //过高门限
    var overLimitValue: Int? = null,
    //告警门限
    var alarmLimitValue: Int? = null,
    //最大幅值与平均值最小差值
    var maxAverageValue: Int? = null,
    //1秒放电周期最小值
    var secondCycleMinValue: Int? = null,
    //1秒最小放电次数
    var secondDischargeMinCount: Int? = null,
    //噪声宽度门限
    var noiseLimit: Int? = null,
    //触发门限值
    var cfLimitValue: Int? = null,
    //低通滤波器
    var lowPassFiltering: Float? = null,
    //高通滤波器
    var highPassFiltering: Float? = null,
    //内同步的同步频率
    var phaseValue: Float? = null,
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readFloat(),
        parcel.readString(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readFloat(),
        parcel.readFloat(),
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readValue(Float::class.java.classLoader) as? Float,
        parcel.readValue(Float::class.java.classLoader) as? Float,
        parcel.readValue(Float::class.java.classLoader) as? Float) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(xwTb)
        parcel.writeInt(autoTb)
        parcel.writeInt(pdJc)
        parcel.writeInt(lyXc)
        parcel.writeInt(xwPy)
        parcel.writeInt(ljTime)
        parcel.writeInt(gdCd)
        parcel.writeInt(maxValue)
        parcel.writeInt(minValue)
        parcel.writeFloat(nTbPl)
        parcel.writeString(fzUnit)
        parcel.writeInt(outVoice)
        parcel.writeInt(autoZy)
        parcel.writeInt(pdDown)
        parcel.writeInt(pdUp)
        parcel.writeInt(maxValue2)
        parcel.writeInt(minValue2)
        parcel.writeInt(fdlUnit)
        parcel.writeFloat(jzRatio)
        parcel.writeFloat(jzOutValue)
        parcel.writeValue(limitValue)
        parcel.writeValue(jjLimitValue)
        parcel.writeValue(overLimitValue)
        parcel.writeValue(alarmLimitValue)
        parcel.writeValue(maxAverageValue)
        parcel.writeValue(secondCycleMinValue)
        parcel.writeValue(secondDischargeMinCount)
        parcel.writeValue(noiseLimit)
        parcel.writeValue(cfLimitValue)
        parcel.writeValue(lowPassFiltering)
        parcel.writeValue(highPassFiltering)
        parcel.writeValue(phaseValue)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<SettingBean> {
        override fun createFromParcel(parcel: Parcel): SettingBean {
            return SettingBean(parcel)
        }

        override fun newArray(size: Int): Array<SettingBean?> {
            return arrayOfNulls(size)
        }
    }

}
