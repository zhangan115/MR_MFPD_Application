package com.mr.mf_pd.application.manager.socket

enum class CommandType(
    val funCode: Byte,//功能码
    val des: String,//功能说明
    val length: Int,//长度 -1表示长度在传回的byte数组中 -2表示长度在传回的数组中 但需要乘4 其他表示定长
) {
    ReadYcData(0x03, "读取全遥测数据", -2),
    ReadSettingValue(0x04, "读取定值数据", -2),
    SendTime(0x06, "发送对时命令", 11),
    SwitchPassageway(0x07, "切换通道", 8),
    RealData(0x08, "主动上送实时数据", -1),
    FdData(0x09, "上送局部放电数据", -1),
    WriteValue(0x10, "写定值", 10),
    SendPulse(0x12, "上送原始脉冲数据", -1),
    ReadPulse(0x13, "读取原始脉冲数据", 8);
}