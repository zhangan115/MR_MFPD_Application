package com.mr.mf_pd.application.service

import android.app.Service
import android.bluetooth.*
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import com.mr.mf_pd.application.utils.ByteUtil
import com.mr.mf_pd.application.utils.ZLog
import java.util.*

class BluetoothLeService : Service() {

    val TAG = "BluetoothLeService"

    private var bluetoothAdapter: BluetoothAdapter? = null

    private val binder = LocalBinder()
    private var bluetoothGatt: BluetoothGatt? = null
    private var writeCharacteristic: BluetoothGattCharacteristic? = null
    private var notifyCharacteristic: BluetoothGattCharacteristic? = null

    private var connectionState = STATE_DISCONNECTED

    val writeCharacterUuid = UUID.fromString("0000c304-0000-1000-8000-00805f9b34fb")
    val notifyCharacterUuid = UUID.fromString("0000c305-0000-1000-8000-00805f9b34fb")
    val notifyCharacterUuid1 = UUID.fromString("0000c306-0000-1000-8000-00805f9b34fb")

    companion object {
        const val ACTION_GATT_CONNECTED =
            "com.mr.bluetooth.le.ACTION_GATT_CONNECTED"
        const val ACTION_GATT_DISCONNECTED =
            "com.mr.bluetooth.le.ACTION_GATT_DISCONNECTED"
        const val ACTION_GATT_SERVICES_DISCOVERED =
            "com.mr.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED"
        const val ACTION_DATA_AVAILABLE =
            "com.mr.bluetooth.le.ACTION_DATA_AVAILABLE"

        private const val STATE_DISCONNECTED = 0
        private const val STATE_CONNECTED = 2

    }

    override fun onBind(intent: Intent?): IBinder {
        ZLog.d(TAG, "onBind")
        return binder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        ZLog.d(TAG, "onUnbind")
        close()
        return super.onUnbind(intent)
    }

    private fun close() {
        bluetoothGatt?.let { gatt ->
            gatt.disconnect()
            gatt.close()
            bluetoothGatt = null
        }
    }

    fun initialize(): Boolean {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter == null) {
            ZLog.e(TAG, "Unable to obtain a BluetoothAdapter.")
            return false
        }
        return true
    }

    fun connect(address: String): Boolean {
        bluetoothAdapter?.let { adapter ->
            try {
                val device = adapter.getRemoteDevice(address)
                bluetoothGatt = device.connectGatt(this, false, bluetoothGattCallback)
            } catch (exception: IllegalArgumentException) {
                ZLog.d(TAG, "Device not found with provided address.")
                return false
            }
            return true
        } ?: run {
            ZLog.d(TAG, "BluetoothAdapter not initialized")
            return false
        }
    }

    private val bluetoothGattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            ZLog.d(TAG, "onConnectionStateChange status = $status newState = $newState")
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                broadcastUpdate(ACTION_GATT_CONNECTED)
                // successfully connected to the GATT Server
                connectionState = STATE_CONNECTED
                bluetoothGatt?.discoverServices()
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                // disconnected from the GATT Server
                connectionState = STATE_DISCONNECTED
                broadcastUpdate(ACTION_GATT_DISCONNECTED)
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            ZLog.d(TAG, "onServicesDiscovered status = $status")
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED)
            } else {
                ZLog.e(TAG, "onServicesDiscovered received: $status")
            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
        ) {
            super.onCharacteristicChanged(gatt, characteristic)
            //当特征中value值发生改变
            ZLog.d(TAG, "onCharacteristicChanged characteristic uuid = " + characteristic?.uuid)
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int,
        ) {
            // 收到的数据
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic)
                val receiveByte = characteristic.value
                ZLog.d(TAG,
                    "Thread Name is " + Thread.currentThread().name + " receiveByte = " + ByteUtil.bytes2HexStr(
                        receiveByte))
            } else {
                ZLog.e(TAG, "onCharacteristicRead status = $status")
            }
        }

        override fun onDescriptorRead(
            gatt: BluetoothGatt?,
            descriptor: BluetoothGattDescriptor?,
            status: Int,
        ) {
            super.onDescriptorRead(gatt, descriptor, status)
            ZLog.d(TAG, "onDescriptorRead descriptor = " + descriptor?.uuid + " status = " + status)
            if (status == BluetoothGatt.GATT_SUCCESS) {

            }
        }

        override fun onDescriptorWrite(
            gatt: BluetoothGatt?,
            descriptor: BluetoothGattDescriptor?,
            status: Int,
        ) {
            super.onDescriptorWrite(gatt, descriptor, status)
            ZLog.d(TAG,
                "onDescriptorWrite descriptor = " + descriptor?.uuid + " status = " + status)
            if (status == BluetoothGatt.GATT_SUCCESS) {

            }
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int,
        ) {
            super.onCharacteristicWrite(gatt, characteristic, status)
            ZLog.d(TAG, "onCharacteristicWrite status = $status")
            if (status == BluetoothGatt.GATT_SUCCESS) {
                // 发送成功

            } else {
                // 发送失败
                ZLog.e(TAG, "onCharacteristicWrite fail--> status = $status")
            }
        }
    }

    fun readCharacteristic(characteristic: BluetoothGattCharacteristic) {
        bluetoothGatt?.readCharacteristic(characteristic) ?: run {
            ZLog.e(TAG, "BluetoothGatt not initialized")
        }
    }

    fun setCharacteristicNotification(
        characteristic: BluetoothGattCharacteristic,
        enabled: Boolean,
    ) {
        ZLog.d(TAG,
            "setCharacteristicNotification characteristic uuid = " + characteristic.uuid + " enable = " + enabled)
        bluetoothGatt?.let { gatt ->
            val enable = gatt.setCharacteristicNotification(characteristic, enabled)
            ZLog.d(TAG, "setCharacteristicNotification $enable")
            notifyCharacteristic = characteristic
        } ?: run {
            ZLog.e(TAG, "BluetoothGatt not initialized")
        }
    }

    fun setWriteCharacteristic(
        characteristic: BluetoothGattCharacteristic,
    ) {
        bluetoothGatt?.let {
            writeCharacteristic = characteristic
        } ?: run {
            ZLog.e(TAG, "BluetoothGatt not initialized")
        }
    }

    fun writeData(data: ByteArray?) {
        writeCharacteristic?.value = data
        val enable = bluetoothGatt?.setCharacteristicNotification(notifyCharacteristic, true)
        ZLog.d(TAG, "setCharacteristicNotification $enable")
        val state = bluetoothGatt?.writeCharacteristic(writeCharacteristic)
        ZLog.d(TAG, "writeCharacteristic $state")
    }

    private fun broadcastUpdate(action: String) {
        val intent = Intent(action)
        sendBroadcast(intent)
    }

    private fun broadcastUpdate(action: String, characteristic: BluetoothGattCharacteristic) {
        val intent = Intent(action)
        ZLog.d(TAG, "broadcastUpdate uuid = " + characteristic.uuid)
        when (characteristic.uuid) {

        }
        sendBroadcast(intent)
    }

    fun getSupportedGattServices(): List<BluetoothGattService>? {
        return bluetoothGatt?.services
    }

    inner class LocalBinder : Binder() {

        fun getService(): BluetoothLeService {
            return this@BluetoothLeService
        }
    }

}