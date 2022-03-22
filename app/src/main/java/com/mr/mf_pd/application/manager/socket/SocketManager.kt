package com.mr.mf_pd.application.manager.socket

import android.util.Log
import androidx.annotation.MainThread
import com.google.common.primitives.Bytes
import com.mr.mf_pd.application.app.MRApplication.Companion.appHost
import com.mr.mf_pd.application.app.MRApplication.Companion.port
import com.mr.mf_pd.application.common.Constants
import com.mr.mf_pd.application.manager.socket.callback.*
import com.mr.mf_pd.application.manager.socket.comand.CommandType
import com.sito.tool.library.utils.ByteLibUtil
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.InetSocketAddress
import java.net.Socket
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList
import kotlin.collections.LinkedHashMap

class SocketManager private constructor() {
    private var inputStream //输入流
            : InputStream? = null
    private var outputStream //输出流
            : OutputStream? = null

    private var mDataByteList: LinkedList<Byte> = LinkedList()

    private var linkStateListeners: MutableList<LinkStateListener>? = null

    private val bytesCallbackMap: LinkedHashMap<CommandType, LinkedList<BytesDataCallback>> =
        LinkedHashMap()

    private var socket: Socket? = null

    companion object {
        private const val DEVICE_NO = 1
        private var isConnected //是否连接
                = false

        private var instance: SocketManager? = null
            get() {
                if (field == null) {
                    field = SocketManager()
                }
                return field
            }

        fun get(): SocketManager {
            return instance!!
        }
    }

    //执行请求任务的线程池
    private var mRequestExecutor: ExecutorService? = null
    private var future: Future<*>? = null

    private val requestRunnable = Runnable {
        try {
            val address = InetSocketAddress(appHost(), port())
            socket = Socket()
            socket?.connect(address, 2000)
            socket?.keepAlive = true
            isConnected = socket!!.isConnected
            inputStream = socket!!.getInputStream()
            outputStream = socket!!.getOutputStream()
            for (i in linkStateListeners!!.indices) {
                linkStateListeners!![i].onLinkState(Constants.LINK_SUCCESS)
            }
            bytesCallbackMap.clear()
            val buf = ByteArray(1024 * 4)
            var size: Int
            mDataByteList.clear()
            while (inputStream!!.read(buf).also { size = it } != -1) {
                try {
                    if (mDataByteList.isNotEmpty()) {
                        Log.d("zhangan", mDataByteList.size.toString())
                    }
                    val sources = ByteArray(size)
                    System.arraycopy(buf, 0, sources, 0, size)
                    mDataByteList.addAll(Bytes.asList(*sources))
                    dealStickyBytes()
                } catch (e: Exception) {
                    e.printStackTrace()
                    mDataByteList.clear()
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
            socket = null
        } finally {
            try {
                mDataByteList.clear()
                mDataByteList.clear()
                isConnected = false
                if (inputStream != null) {
                    inputStream!!.close()
                }
                if (outputStream != null) {
                    outputStream!!.close()
                }
                if (socket != null) {
                    socket!!.close()
                }
                for (i in linkStateListeners!!.indices) {
                    linkStateListeners!![i].onLinkState(Constants.LINK_FAIL)
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun dealStickyBytes() {
        var length = -1
        var commandType: CommandType? = null
        if (mDataByteList[0].toInt() == DEVICE_NO && mDataByteList.size > 4) {
            //根据数据获取命令类型
            commandType = CommandType.values().firstOrNull { it.funCode == mDataByteList[1] }
            when (commandType) {
                CommandType.SendTime -> {
                    if (mDataByteList.size >= commandType.length) {
                        length = commandType.length
                    }
                }
                CommandType.SwitchPassageway -> {
                    if (mDataByteList.size >= commandType.length) {
                        length = commandType.length
                    }
                }
                CommandType.ReadYcData -> {
                    length = mDataByteList[2].toInt() * 4 + 5
                }
                CommandType.ReadSettingValue -> {
                    length = mDataByteList[2].toInt() * 4 + 5
                }
                CommandType.WriteValue -> {
                    length = mDataByteList[2].toInt() + 5
                }
                CommandType.FdData -> {
                    mDataByteList.clear()
//                    val lengthBytes = byteArrayOf(0x00, 0x00, byteList[2], byteList[3])
//                    val length = ByteLibUtil.getInt(lengthBytes) + 2
//                    byteList.removeAll(handOut(byteList, length))
                }
                CommandType.RealData -> {
                    val lengthBytes = byteArrayOf(0x00, 0x00, mDataByteList[3], mDataByteList[4])
                    length = ByteLibUtil.getInt(lengthBytes) * 6 + 7
                }
                CommandType.FlightValue -> {
                    val lengthBytes = byteArrayOf(0x00, 0x00, mDataByteList[3], mDataByteList[4])
                    length = ByteLibUtil.getInt(lengthBytes) * 6 + 7
                }
                else -> {
                    mDataByteList.clear()
                    Log.d("zhangan", "不支持的命令参数")
                }
            }
        }
        if (length > 0 && length <= mDataByteList.size) {
            val list = mDataByteList.subList(0, length)
            handOut(commandType, Bytes.toArray(list))
            val newList = LinkedList<Byte>()
            for (i in length until mDataByteList.size) {
                newList.add(mDataByteList[i])
            }
            mDataByteList = newList
            if (mDataByteList.size > 0) {
                dealStickyBytes()
            }
        }
    }

    private fun handOut(commandType: CommandType?, source: ByteArray) {
        if (commandType != null) {
            bytesCallbackMap[commandType]?.forEach {
                it.onData(source)
            }
        } else {
            Log.i("zhangan", "commandType is null ")
        }
    }

    /**
     * 销毁socket
     */
    private fun destroy() {
        try {
            socket?.close()
            inputStream?.close()
            outputStream?.close()
            linkStateListeners?.clear()
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            socket = null
        }
    }

    /**
     * 释放请求线程
     */
    fun releaseRequest() {
        destroy()
        future?.let {
            if (it.isCancelled) {
                it.cancel(true)
            }
        }
        if (mRequestExecutor != null && !mRequestExecutor!!.isShutdown) {
            mRequestExecutor!!.shutdownNow()
            mRequestExecutor = null
        }
    }

    @MainThread
    @Synchronized
    fun addCallBack(commandType: CommandType, callback: BytesDataCallback) {
        if (bytesCallbackMap.containsKey(commandType)) {
            if (bytesCallbackMap[commandType] == null) {
                bytesCallbackMap[commandType] = LinkedList()
            }
            bytesCallbackMap[commandType]!!.add(callback)
        } else {
            val linkedList = LinkedList<BytesDataCallback>()
            linkedList.add(callback)
            bytesCallbackMap[commandType] = linkedList
        }
    }

    @MainThread
    @Synchronized
    fun removeCallBack(commandType: CommandType, callback: BytesDataCallback) {
        if (bytesCallbackMap.containsKey(commandType)) {
            if (bytesCallbackMap[commandType] != null
                && bytesCallbackMap[commandType]!!.contains(callback)
            ) {
                bytesCallbackMap[commandType]!!.remove(callback)
            }
        }
    }

    @MainThread
    @Synchronized
    fun removeCallBack(commandType: CommandType) {
        if (bytesCallbackMap.containsKey(commandType)) {
            bytesCallbackMap.remove(commandType)
        }
    }

    /**
     * socket 连接
     */
    fun initLink() {
        mRequestExecutor = Executors.newSingleThreadExecutor()
        future = mRequestExecutor?.submit(requestRunnable)
    }

    /**
     * 增加状态监控
     *
     * @param listener 状态监控
     */
    fun addLinkStateListeners(listener: LinkStateListener) {
        if (linkStateListeners == null) {
            linkStateListeners = ArrayList()
        }
        linkStateListeners!!.add(listener)
    }

    /**
     * 无需返回的发送数据
     *
     * @param data 数据
     * @return 订阅
     */
    @Synchronized
    fun sendData(data: ByteArray?): Disposable {
        return Observable.create { emitter: ObservableEmitter<Boolean?> ->
            try {
                if (outputStream != null && socket != null && !socket!!.isClosed && data != null) {
                    outputStream!!.write(data)
                    outputStream!!.flush()
                    emitter.onNext(true)
                    Log.d("zhangan", "send data " + Bytes.asList(*data).toString())
                } else {
                    Log.d("zhangan", "发送失败")
                    emitter.onNext(false)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                emitter.onError(e)
            } finally {
                emitter.onComplete()
            }
        }.subscribeOn(Schedulers.io()).subscribe()
    }

    @Synchronized
    fun sendRepeatData(
        data: ByteArray?,
        time: Long = 10,
        unit: TimeUnit = TimeUnit.SECONDS,
    ): Disposable {
        return Observable.create { emitter: ObservableEmitter<Boolean?> ->
            try {
                if (outputStream != null && socket != null && !socket!!.isClosed && data != null) {
                    outputStream!!.write(data)
                    outputStream!!.flush()
                    emitter.onNext(true)
                } else {
                    Log.d("zhangan", "发送失败")
                    emitter.onNext(false)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                emitter.onError(e)
            } finally {
                emitter.onComplete()
            }
        }.repeatWhen { objectObservable: Observable<Any?> ->
            objectObservable.delay(time, unit)
        }.subscribeOn(Schedulers.io()).subscribe()
    }

}