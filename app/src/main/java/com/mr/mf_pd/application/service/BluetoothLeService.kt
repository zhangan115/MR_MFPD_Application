package com.mr.mf_pd.application.service

import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothProfile
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import com.mr.mf_pd.application.utils.ZLog

class BluetoothLeService : Service() {

    val TAG = "BluetoothLeService"

    private var bluetoothAdapter: BluetoothAdapter? = null

    private val binder = LocalBinder()
    private var bluetoothGatt: BluetoothGatt? = null

    private var connectionState = STATE_DISCONNECTED

    companion object {
        const val ACTION_GATT_CONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_CONNECTED"
        const val ACTION_GATT_DISCONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED"

        private const val STATE_DISCONNECTED = 0
        private const val STATE_CONNECTED = 2

    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        close()
        return super.onUnbind(intent)
    }

    private fun close() {
        bluetoothGatt?.let { gatt ->
            gatt.close()
            bluetoothGatt = null
        }
    }

    fun initialize():Boolean {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter == null){
            ZLog.e(TAG,"Unable to obtain a BluetoothAdapter.")
            return false
        }
        return true
    }

    fun connect(address: String): Boolean {
        bluetoothAdapter?.let { adapter ->
            try {
                val device = adapter.getRemoteDevice(address)
                bluetoothGatt = device.connectGatt(this,false,bluetoothGattCallback)
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
                // successfully connected to the GATT Server
                connectionState = STATE_CONNECTED
                broadcastUpdate(ACTION_GATT_CONNECTED)
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                // disconnected from the GATT Server
                connectionState = STATE_DISCONNECTED
                broadcastUpdate(ACTION_GATT_DISCONNECTED)
            }
        }
    }

    private fun broadcastUpdate(action: String) {
        val intent = Intent(action)
        sendBroadcast(intent)
    }

    inner class LocalBinder:Binder(){

        fun getService():BluetoothLeService{
            return this@BluetoothLeService
        }
    }

}