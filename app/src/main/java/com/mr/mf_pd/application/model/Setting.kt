package com.mr.mf_pd.application.model

open class BaseSettingBean {
    var xwTb: Int = 0//相位同步
    var autoTb: Int = 0//自动同步
    var lyXc: Int = 0//滤音消除
    var xwPy: Int = 0//相位偏移
    var ljTime: Int = 0//图谱累积时间
    var gdCd: Int = 0//固定尺度
    var maxValue: Int = 0//最大幅值
    var minValue: Int = 0//最小幅值
}

class UHFSettingBean : BaseSettingBean() {
    var nTbPl: Int = 0//内同步频率
}

class TEVSettingBean : BaseSettingBean() {
    var nTbPl: Int = 0//内同步频率
}

class ACSettingBean : BaseSettingBean() {
    var fzUnit: String = "mV"//幅值单位
    var nTbPl: Int = 0//内同步频率
    var outVoice: Boolean = false //声音输出
    var pdDown: Int = 50 //频带下限
    var pdUp: Int = 300 //频带上限
    var maxValue2: Int = 0//最大幅值2
    var minValue2: Int = 0//最小幅值2
}

class HFSettingBean : BaseSettingBean() {
    var fdlUnit: Int = 0 //以放电量单位
    var jzRatio: Float = 1.0f//校准系数
    var jzOutValue: Float = 10.0f//校准器输出
}

data class SettingBean(
    val cacheKey:String,//缓存的Key
    var xwTb: Int = 0,//相位同步
    var autoTb: Int = 0,//自动同步
    var pdJc: Int = 0,//频带检测
    var lyXc: Int = 0,//滤音消除
    var xwPy: Int = 0,//相位偏移
    var ljTime: Int = 0,//图谱累积时间
    var gdCd: Int = 0,//固定尺度
    var maxValue: Int = 0,//最大幅值
    var minValue: Int = 0,//最小幅值

    var nTbPl: Float = 0f,//内同步频率
    var fzUnit: String = "mV",//幅值单位
    var outVoice: Int = 0, //声音输出
    var pdDown: Int = 50, //频带下限
    var pdUp: Int = 300, //频带上限
    var maxValue2: Int = 0,//最大幅值2
    var minValue2: Int = 0,//最小幅值2

    var fdlUnit: Int = 0, //以放电量单位
    var jzRatio: Float = 1.0f,//校准系数
    var jzOutValue: Float = 10.0f,//校准器输出
)
