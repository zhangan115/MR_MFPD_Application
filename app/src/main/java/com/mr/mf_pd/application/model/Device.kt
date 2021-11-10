package com.mr.mf_pd.application.model

import android.os.Parcel
import android.os.Parcelable
import androidx.databinding.ObservableField

/**
 * 设备类
 */
data class DeviceBean(
    var deviceName: String?,//设备名称
    var serialNo: String?,//序列号
    var rSSI: Int,//RSSI
    var power: Int,//电量
    var powerState: Int,//电量类型 0低电量 1高电量
    var versionCode: String?,//版本号
    var deviceType: Int,//设备类型
    var bssid:String?
) : Parcelable {

    val deviceNameAttr: ObservableField<String> = ObservableField(deviceName)
    val serialNoAttr: ObservableField<String> = ObservableField("序列号：$serialNo")
    val rSSIAttr: ObservableField<String> = ObservableField("${rSSI}dbm")
    val powerAttr: ObservableField<String> = ObservableField("$power%")
    val powerStateAttr: ObservableField<Int> = ObservableField(powerState)
    val deviceTypeAttr: ObservableField<Int> = ObservableField(deviceType)
    val versionCodeAttr: ObservableField<String> = ObservableField("版本号：$versionCode")

    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readString(),
        parcel.readInt(),
        parcel.readString()
        ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(deviceName)
        parcel.writeString(serialNo)
        parcel.writeInt(rSSI)
        parcel.writeInt(power)
        parcel.writeInt(powerState)
        parcel.writeString(versionCode)
        parcel.writeInt(deviceType)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<DeviceBean> {
        override fun createFromParcel(parcel: Parcel): DeviceBean {
            return DeviceBean(parcel)
        }

        override fun newArray(size: Int): Array<DeviceBean?> {
            return arrayOfNulls(size)
        }
    }
}