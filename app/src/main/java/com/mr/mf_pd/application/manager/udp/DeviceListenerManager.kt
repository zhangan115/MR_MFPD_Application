package com.mr.mf_pd.application.manager.udp

import android.os.Handler
import android.os.Message
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.util.*

/**
 * 使用UDP来监听手持终端
 * 监听端口 7277
 * @since 2022-06-01
 * @author anson
 */
object DeviceListenerManager {

    const val DEVICE_NUM = 1
    const val ERROR_NUM = 2
    const val START_NUM = 3

    private var thread: Thread? = null
    private var socket: DatagramSocket? = null
    private val callback: Vector<UDPListener> = Vector()

    private class DeviceHandle : Handler() {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when (msg.what) {
                DEVICE_NUM -> {
                    val byteArray = msg.obj as ByteArray
                    callback.forEach {
                        it.onData(byteArray)
                    }
                }
                ERROR_NUM -> {
                    callback.forEach {
                        it.onError()
                    }
                }
                START_NUM -> {
                    callback.forEach {
                        it.onStart()
                    }
                }
            }
        }
    }

    private val handle = DeviceHandle()

    fun startListener() {
        if (thread != null) {
            return
        }
        thread = DeviceListenerThread()
        thread?.start()
    }

    private class DeviceListenerThread : Thread() {
        override fun run() {
            super.run()
            socket = DatagramSocket(7277)
            val buff = ByteArray(1024)
            while (!isInterrupted) {
                try {
                    val dgp = DatagramPacket(buff, buff.size)
                    socket?.receive(dgp)
                    val length = dgp.length
                    val resultByteArray = ByteArray(length)
                    System.arraycopy(buff, 0, resultByteArray, 0, length)
                    val message = Message()
                    message.what = DEVICE_NUM
                    message.obj = resultByteArray
                    handle.sendMessage(message)
                } catch (e: Exception) {
                    e.printStackTrace()
                    handle.sendEmptyMessage(ERROR_NUM)
                }
            }
        }
    }

    fun addListener(listener: UDPListener) {
        if (!callback.contains(listener)) {
            callback.add(listener)
        }
    }

    fun removeListener(listener: UDPListener) {
        callback.remove(listener)
    }

    private fun cleanListener() {
        callback.clear()
    }

    fun disableListener() {
        cleanListener()
        thread?.interrupt()
        thread = null
        socket?.close()
        socket = null
    }
}