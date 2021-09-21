package com.mr.mf_pd.application.manager

import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.Socket
import java.util.*

object SocketManager {

    private var outputStream //输出流
            : OutputStream? = null
    private var inputStream //输入流
            : InputStream? = null
    private var readListeners: MutableList<ReadListener>? = null

    var isConnected: Boolean = false

    private const val host = "192.168.0.55" //请求地址
    private const val port = 502 //端口
    private lateinit var socket: Socket
    private const val deviceNo = 1

    /**
     * socket 连接
     */
    fun initLink() {
        Thread {
            val inputStream = socket.getInputStream()
            this.inputStream = inputStream
            outputStream = socket.getOutputStream()
            try {
                socket = Socket(host, port)
                socket.keepAlive = true
                isConnected = socket.isConnected
                val buf = ByteArray(1024 * 2)
                var size: Int? = 0
                while (true) {
                    if (inputStream?.read(buf).also { size = it } != -1) {
                        break
                    }
                    if (readListeners != null && readListeners!!.isNotEmpty()) {
                        for (listener in readListeners!!) {
                            listener.onRead(buf)
                        }
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                try {
                    isConnected = false
                    socket.close()
                    inputStream?.close()
                    if (outputStream != null) {
                        outputStream!!.close()
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }.start()
    }

    fun addReadListener(listener: ReadListener) {
        if (readListeners == null) {
            readListeners = ArrayList()
        }
        readListeners!!.add(listener)
    }

    fun getReadListeners(): List<ReadListener>? {
        return readListeners
    }

    fun setReadListeners(readListeners: MutableList<ReadListener>?) {
        this.readListeners = readListeners
    }

    fun removeListener(listener: ReadListener?) {
        if (this.readListeners != null && listener != null) {
            if (this.readListeners!!.contains(listener)) {
                this.readListeners!!.remove(listener)
            }
        }
    }

    /**
     * 销毁socket
     */
    fun destroy() {
        try {
            socket.close()
            inputStream?.close()
            outputStream?.close()
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {

        }
    }


}