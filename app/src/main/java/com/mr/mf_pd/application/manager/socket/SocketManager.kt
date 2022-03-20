package com.mr.mf_pd.application.manager.socket

import android.util.Log
import com.google.common.primitives.Bytes
import com.mr.mf_pd.application.app.MRApplication.Companion.appHost
import com.mr.mf_pd.application.app.MRApplication.Companion.port
import com.mr.mf_pd.application.common.Constants
import com.mr.mf_pd.application.manager.socket.callback.*
import com.mr.mf_pd.application.manager.socket.comand.CommandType
import com.sito.tool.library.utils.ByteLibUtil
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.ObservableOnSubscribe
import io.reactivex.android.schedulers.AndroidSchedulers
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

class SocketManager private constructor() {
    private var inputStream //输入流
            : InputStream? = null
    private var outputStream //输出流
            : OutputStream? = null

    private val mPulseDataListener: PulseDataListener? = null
    private var linkStateListeners: MutableList<LinkStateListener>? = null

    private val writeSettingCallbacks: ArrayList<WriteSettingDataCallback> = ArrayList()
    private val readSettingCallbacks: ArrayList<ReadSettingDataCallback> = ArrayList()

    private var readDataCallback: ReadListener? = null
    var ycDataCallback: YcDataCallback? = null
    var sendTimeCallback: BaseDataCallback? = null
    var openPassageway: BaseDataCallback? = null

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

    fun addWriteSettingCallback(callback: WriteSettingDataCallback) {
        writeSettingCallbacks.add(callback)
    }

    fun removeWriteSettingCallback(callback: WriteSettingDataCallback) {
        writeSettingCallbacks.remove(callback)
    }

    fun addReadSettingCallback(callback: ReadSettingDataCallback) {
        readSettingCallbacks.add(callback)
    }

    fun removeReadSettingCallback(callback: ReadSettingDataCallback) {
        readSettingCallbacks.remove(callback)
    }

    //执行请求任务的线程池
    private var mRequestExecutor: ExecutorService? = null
    private var future: Future<*>? = null

    private val requestRunnable = Runnable {
        try {
            val address = InetSocketAddress(appHost(), port())
            socket = Socket()
            socket!!.connect(address, 2000)
            socket!!.keepAlive = true
            isConnected = socket!!.isConnected
            inputStream = socket!!.getInputStream()
            outputStream = socket!!.getOutputStream()
            for (i in linkStateListeners!!.indices) {
                linkStateListeners!![i].onLinkState(Constants.LINK_SUCCESS)
            }
            val buf = ByteArray(1024 * 4)
            val byteList: MutableList<Byte> = ArrayList()
            var size: Int
            while (inputStream!!.read(buf).also { size = it } != -1) {
                try {
                    if (size < buf.size) {
                        byteList.clear()
                    }
                    val sources = ByteArray(size)
                    System.arraycopy(buf, 0, sources, 0, size)
                    byteList.addAll(Bytes.asList(*sources))
                    dealStickyBytes(byteList)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
            socket = null
        } finally {
            try {
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

    /***
     * 处理连包
     * @param byteList 字节集合
     */
    private fun dealStickyBytes(byteList: MutableList<Byte>) {
        var dealByteList: MutableList<Byte> = ArrayList()
        if (byteList.size > 3 && byteList[0].toInt() == DEVICE_NO) {
            //对定长的数据进行单独处理
            when (val commandType =
                CommandType.values().firstOrNull { it.funCode == byteList[1] }) {
                CommandType.SendTime -> {
                    if (byteList.size >= commandType.length) {
                        handOut(byteList, commandType.length)
                        dealByteList = byteList.subList(commandType.length, byteList.size)
                    }
                }
                CommandType.SwitchPassageway -> {
                    if (byteList.size >= commandType.length) {
                        handOut(byteList, commandType.length)
                        dealByteList = byteList.subList(commandType.length, byteList.size)
                    }
                }
                CommandType.ReadYcData -> {
                    val length = byteList[2].toInt() * 4 + 5
                    handOut(byteList, length)
                    dealByteList = byteList.subList(length, byteList.size)
                }
                CommandType.ReadSettingValue -> {
                    val length = byteList[2].toInt() * 4 + 5
                    handOut(byteList, length)
                    dealByteList = byteList.subList(length, byteList.size)
                }
                CommandType.WriteValue -> {
                    val length = byteList[2].toInt() + 5
                    handOut(byteList, length)
                    dealByteList = byteList.subList(length, byteList.size)
                }
                CommandType.FdData -> {
//                    val lengthBytes = byteArrayOf(0x00, 0x00, byteList[2], byteList[3])
//                    val length = ByteLibUtil.getInt(lengthBytes) + 2
//                    byteList.removeAll(handOut(byteList, length))
                }
                CommandType.RealData -> {
                    val lengthBytes = byteArrayOf(0x00, 0x00, byteList[3], byteList[4])
                    val length = ByteLibUtil.getInt(lengthBytes) * 6 + 7
                    handOut(byteList, length)
                    dealByteList = byteList.subList(length, byteList.size)
                }
                else -> {
                    byteList.clear()
                }
            }
        } else {
            byteList.clear()
        }
        if (dealByteList.size > 0) {
            dealStickyBytes(dealByteList)
        } else {
            byteList.clear()
        }
    }

    private fun handOut(byteList: List<Byte>, length: Int) {
        val source = Bytes.toArray(byteList.subList(0, length))
        when (CommandType.values().firstOrNull { it.funCode == source[1] }) {
            CommandType.SendTime -> {
                sendTimeCallback?.onData(source)
            }
            CommandType.SwitchPassageway -> {
                openPassageway?.onData(source)
            }
            CommandType.ReadYcData -> {
                ycDataCallback?.onData(source)
            }
            CommandType.ReadSettingValue -> {
                readSettingCallbacks.forEach {
                    it.onData(source)
                }
            }
            CommandType.WriteValue -> {
                writeSettingCallbacks.forEach {
                    it.onData(source)
                }
            }
            CommandType.FdData -> {

            }
            CommandType.SendPulse -> {
                mPulseDataListener?.onRead(source)
            }
            CommandType.RealData -> {
                readDataCallback?.onData(source)
            }
            else -> {

            }
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
     * 发送数据
     *
     * @param data     数据
     * @param cmdType  命令类型
     * @param callback 回调
     */
    @Synchronized
    fun sendData(data: ByteArray?, cmdType: CommandType, callback: ReceiverCallback?): Disposable {
        return Observable.create(ObservableOnSubscribe { emitter: ObservableEmitter<ByteArray> ->
            try {
                if (outputStream != null && socket != null && !socket!!.isClosed && data != null) {
                    outputStream!!.write(data)
                    outputStream!!.flush()
                } else {
                    emitter.onComplete()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                emitter.onError(e)
            }
        } as ObservableOnSubscribe<ByteArray>)
            .timeout(5, TimeUnit.SECONDS)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ bytes: ByteArray? -> callback?.onReceiver(bytes) }) { obj: Throwable -> obj.printStackTrace() }
    }

    /**
     * 无需返回的发送数据
     *
     * @param data 数据
     * @return 订阅
     */
    @Synchronized
    fun sendData(data: ByteArray?): Disposable {
        Log.d("zhangan","sendData "+ByteLibUtil.bytes2HexStr(data))
        return Observable.create { emitter: ObservableEmitter<Boolean?> ->
            try {
                Log.d("zhangan", "send data thread name is  ${Thread.currentThread().name}")
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

    /**
     * 移除状态监控
     *
     * @param listener 状态监控
     */
    fun removeLinkStateListener(listener: LinkStateListener?) {
        linkStateListeners?.remove(listener)
    }

    /**
     * 增加读取监控
     *
     * @param listener 读取监控
     */
    fun setReadListener(listener: ReadListener?) {
        readDataCallback = listener
    }

    /**
     * 移除读取回调
     */
    fun removeReadListener() {
        readDataCallback = null
    }

}