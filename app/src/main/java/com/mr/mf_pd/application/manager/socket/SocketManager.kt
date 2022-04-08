package com.mr.mf_pd.application.manager.socket

import android.util.Log
import androidx.annotation.MainThread
import com.google.common.primitives.Bytes
import com.mr.mf_pd.application.app.MRApplication
import com.mr.mf_pd.application.app.MRApplication.Companion.appHost
import com.mr.mf_pd.application.app.MRApplication.Companion.port
import com.mr.mf_pd.application.common.Constants
import com.mr.mf_pd.application.manager.socket.callback.*
import com.mr.mf_pd.application.manager.socket.comand.CommandType
import com.mr.mf_pd.application.utils.DateUtil
import com.sito.tool.library.utils.ByteLibUtil
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.io.*
import java.net.InetSocketAddress
import java.net.Socket
import java.util.*
import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicBoolean
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

    //开辟100Kb的存储空间 用来保存数据
    private val dataBuffer = ByteArray(1024 * 100)

    var realDataDeque: ArrayBlockingQueue<ByteArray>? = null//实时数据队列
    var flightDeque: ArrayBlockingQueue<ByteArray>? = null//飞行数据队列
    var fdDataDeque: ArrayBlockingQueue<ByteArray>? = null//放电数据队列
    var pulseDataDeque: ArrayBlockingQueue<ByteArray>? = null// 原始脉冲数据队列

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

    private val dataQueue: ArrayBlockingQueue<ByteArray> = ArrayBlockingQueue(50)

    //执行请求任务的线程池
    private var mRequestExecutor: ExecutorService? = null
    private var future: Future<*>? = null

    private var mQueueExecutor: ExecutorService? = null
    private var mQueueFuture: Future<*>? = null

    private var fos: FileOutputStream? = null

    @Volatile
    private var isExecuting = true

    /**
     *
     */
    private fun startQueueTask() {
        val dir = MRApplication.instance.fileCacheFile()
        val mrDir = File(dir, "Mr")
        if (!mrDir.exists()) {
            mrDir.mkdir()
        }
        val file = File(mrDir,
            DateUtil.timeFormat(System.currentTimeMillis(), "yyyy_MM_dd_HH_mm_ss") + ".txt")
        file.createNewFile()
        Log.d("zhangan", file.absolutePath)
        fos = FileOutputStream(file, true)
    }

    private fun cleanAllData() {
        mDataByteList.clear()
        dataQueue.clear()
    }

    private val requestRunnable = Runnable {
        try {
            val address = InetSocketAddress(appHost(), port())
            socket = Socket()
            socket?.let { socket ->
                socket.connect(address, 2000)
                socket.keepAlive = true
                isConnected = socket.isConnected
                inputStream = socket.getInputStream()
                outputStream = socket.getOutputStream()
                linkStateListeners?.forEach {
                    it.onLinkState(Constants.LINK_SUCCESS)
                }
            }
            var size: Int
//            startQueueTask()
            inputStream?.let { inputStream ->
                while (inputStream.read(dataBuffer).also { size = it } != -1) {
                    try {
                        Log.d("zhangan", "read size is $size")
                        val sources = ByteArray(size)
                        System.arraycopy(dataBuffer, 0, sources, 0, size)
                        val isSuccess = dataQueue.offer(sources)
                        if (!isSuccess) {
                            dataQueue.clear()
                            dataQueue.offer(sources)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
            socket = null
        } finally {
            try {
                isExecuting = false
                dataQueue.offer(null)
                mDataByteList.clear()
                isConnected = false
                inputStream?.close()
                outputStream?.close()
                socket?.close()
                linkStateListeners?.forEach { it.onLinkState(Constants.LINK_FAIL) }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun dealByteList() {
        var length = -1
        var commandType: CommandType? = null
        if (mDataByteList[0].toInt() == DEVICE_NO && mDataByteList.size > 5) {
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
                    val lengthBytes = byteArrayOf(0x00, 0x00, mDataByteList[2], mDataByteList[3])
                    length = ByteLibUtil.getInt(lengthBytes) + 4
                }
                CommandType.SendPulse -> {
                    val lengthBytes =
                        byteArrayOf(0x00, mDataByteList[2], mDataByteList[3], mDataByteList[4])
                    length = ByteLibUtil.getInt(lengthBytes) + 7
                }
                CommandType.RealData -> {
                    val lengthBytes = byteArrayOf(0x00, 0x00, mDataByteList[3], mDataByteList[4])
                    length = ByteLibUtil.getInt(lengthBytes) * 6 + 7
                }
                CommandType.FlightValue -> {
                    val lengthBytes = byteArrayOf(0x00, 0x00, mDataByteList[3], mDataByteList[4])
                    length = ByteLibUtil.getInt(lengthBytes) * 6 + 7
                }
            }
        }
        if (length > 0 && length <= mDataByteList.size) {
            val list = mDataByteList.subList(0, length)
            commandCallback(commandType, Bytes.toArray(list))
            val newList = LinkedList<Byte>()
            for (i in length until mDataByteList.size) {
                newList.add(mDataByteList[i])
            }
            mDataByteList = newList
            if (mDataByteList.size > 0) {
                dealByteList()
            }
        }
    }

    private fun commandCallback(commandType: CommandType?, source: ByteArray) {
        if (commandType != null) {
            when (commandType) {
                CommandType.FlightValue -> {
                    flightDeque?.let {
                        val isSuccess = it.offer(source)
                        if (!isSuccess) {
                            it.clear()
                        }
                    }
                }
                CommandType.RealData -> {
                    realDataDeque?.let {
                        val isSuccess = it.offer(source)
                        if (!isSuccess) {
                            it.clear()
                        }
                    }
                }
                CommandType.FdData -> {
                    fdDataDeque?.let {
                        val isSuccess = it.offer(source)
                        if (!isSuccess) {
                            it.clear()
                        }
                    }
                }
                CommandType.SendPulse -> {
                    pulseDataDeque?.let {
                        val isSuccess = it.offer(source)
                        if (!isSuccess) {
                            it.clear()
                        }
                    }
                }
                else -> {
                    bytesCallbackMap[commandType]?.forEach {
                        it.onData(source)
                    }
                }
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
            mDataByteList.clear()
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            socket = null
        }
    }

    @MainThread
    @Synchronized
    fun cleanBytesCallbackMap() {
        bytesCallbackMap.clear()
    }

    /**
     * 释放请求线程
     */
    fun releaseRequest() {
        destroy()
        if (mRequestExecutor != null && !mRequestExecutor!!.isShutdown) {
            mRequestExecutor!!.shutdownNow()
            mRequestExecutor = null
        }
        future?.let {
            if (it.isCancelled) {
                it.cancel(true)
            }
        }
        isQueueRuing.set(false)
        if (mQueueExecutor != null && !mQueueExecutor!!.isShutdown) {
            mQueueExecutor?.shutdownNow()
            mQueueExecutor = null
        }
        mQueueFuture?.let {
            if (it.isCancelled) {
                it.cancel(true)
            }
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

    private var isQueueRuing: AtomicBoolean = AtomicBoolean(true)

    /**
     * socket 连接
     */
    fun initLink() {
        cleanAllData()
        isExecuting = true
        realDataDeque = ArrayBlockingQueue<ByteArray>(50)
        flightDeque = ArrayBlockingQueue<ByteArray>(50)
        fdDataDeque = ArrayBlockingQueue<ByteArray>(50)
        pulseDataDeque = ArrayBlockingQueue<ByteArray>(50)

        mRequestExecutor = Executors.newSingleThreadExecutor()
        future = mRequestExecutor?.submit(requestRunnable)

        mQueueExecutor = Executors.newSingleThreadExecutor()
        mQueueFuture = mQueueExecutor?.submit {
            try {
                while (isExecuting) {
                    val dataList = dataQueue.take()
                    dataList?.let {
                        mDataByteList.addAll(Bytes.asList(*it))
                        dealByteList()
//                        fos?.write((ByteLibUtil.bytes2HexStr(it) + "\n").toByteArray())
//                        fos?.flush()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                try {
                    fos?.close()
                    Log.d("zhangan", "file close")
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }
        }
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