package com.mr.mf_pd.application.model

import androidx.databinding.ObservableField

/**
 * 设备类
 */
class DeviceBean(
    deviceName: String,//设备名称
    serialNo: String,//序列号
    rSSI: Int,//RSSI
    power: Int,//电量
    powerState: Int,//电量类型 0低电量 1高电量
    versionCode: String,//版本号
    deviceType: Int//设备类型
) {
    val deviceName: ObservableField<String> = ObservableField(deviceName)
    val serialNo: ObservableField<String> = ObservableField("序列号：$serialNo")
    val rSSI: ObservableField<String> = ObservableField("${rSSI}dbm")
    val power: ObservableField<String> = ObservableField("$power%")
    val powerState: ObservableField<Int> = ObservableField(powerState)
    val deviceType: ObservableField<Int> = ObservableField(deviceType)
    val versionCode: ObservableField<String> = ObservableField("版本号：$versionCode")
}