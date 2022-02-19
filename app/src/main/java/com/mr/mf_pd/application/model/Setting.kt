package com.mr.mf_pd.application.model

data class SettingBean(
    val cacheKey: String,//缓存的Key
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
    var fzUnit: Int = 0,//幅值单位
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
)
