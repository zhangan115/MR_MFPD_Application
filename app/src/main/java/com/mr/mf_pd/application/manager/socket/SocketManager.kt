package com.mr.mf_pd.application.manager.socket

import android.util.Log
import androidx.annotation.MainThread
import com.google.common.primitives.Bytes
import com.mr.mf_pd.application.app.MRApplication.Companion.appHost
import com.mr.mf_pd.application.app.MRApplication.Companion.port
import com.mr.mf_pd.application.common.ConstantStr
import com.mr.mf_pd.application.common.Constants
import com.mr.mf_pd.application.manager.socket.callback.BytesDataCallback
import com.mr.mf_pd.application.manager.socket.callback.LinkStateListener
import com.mr.mf_pd.application.manager.socket.comand.CommandType
import com.mr.mf_pd.application.utils.ByteUtil
import com.mr.mf_pd.application.utils.toHexString
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

    //连接上的设备序列号
    var linkedDeviceSerialNo: String? = null

    private var mDataByteList: LinkedList<Byte> = LinkedList()

    private var linkStateListeners: MutableList<LinkStateListener>? = null

    private val bytesCallbackMap: LinkedHashMap<CommandType, LinkedList<BytesDataCallback>> =
        LinkedHashMap()

    private var socket: Socket? = null

    //开辟100Kb的存储空间 用来保存数据
    private val dataBuffer = ByteArray(1024 * 100)

    var realDataDeque: ArrayBlockingQueue<ByteArray>? = null//实时数据队列
    var flightDeque: ArrayBlockingQueue<ByteArray>? = null//飞行数据队列
    var pulseDataDeque: ArrayBlockingQueue<ByteArray>? = null// 原始脉冲数据队列

    private var ycFw: FileWriter? = null
    private var realFw: FileWriter? = null
    private var flightFw: FileWriter? = null
    private var pulseFw: FileWriter? = null

    companion object {
        private var host: String? = null
        private const val DEVICE_NO = 1

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

    private var mIsSaveData2File = false

    fun setSaveDataFile(file: File?) {
        if (file != null && file.exists()) {
            val ycFile = File(file, ConstantStr.CHECK_YC_FILE_NAME)
            if (!ycFile.exists()) {
                ycFile.createNewFile()
            }
            ycFw = FileWriter(ycFile, true)

            val realFile = File(file, ConstantStr.CHECK_REAL_DATA)
            if (!realFile.exists()) {
                realFile.createNewFile()
            }
            realFw = FileWriter(realFile, true)

            val flightFile = File(file, ConstantStr.CHECK_FLIGHT_FILE_NAME)
            if (!flightFile.exists()) {
                flightFile.createNewFile()
            }
            flightFw = FileWriter(flightFile, true)

            val pulseFile = File(file, ConstantStr.CHECK_PULSE_FILE_NAME)
            if (!pulseFile.exists()) {
                pulseFile.createNewFile()
            }
            pulseFw = FileWriter(pulseFile, true)

            mIsSaveData2File = true
        }
    }

    fun stopSaveData() {
        mIsSaveData2File = false
        try {
            ycFw?.close()
            realFw?.close()
            flightFw?.close()
            pulseFw?.close()
        } catch (e: IOException) {
            e.printStackTrace()
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

    private fun cleanAllData() {
        mDataByteList.clear()
        dataQueue.clear()
    }

    fun getConnection(): Boolean {
        return if (socket == null) false else socket!!.isConnected
    }

    private val requestRunnable = Runnable {
        try {
            val host = if (host == null) appHost() else host
            val address = InetSocketAddress(host, port())
            socket = Socket()
            if (socket != null) {
                socket!!.connect(address, 2000)
                socket!!.keepAlive = false
                inputStream = socket!!.getInputStream()
                outputStream = socket!!.getOutputStream()
                linkStateListeners?.forEach {
                    it.onLinkState(Constants.LINK_SUCCESS)
                }
            }
            var size: Int
            inputStream?.let { inputStream ->
                while (inputStream.read(dataBuffer).also { size = it } != -1) {
                    try {
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
                linkStateListeners?.forEach { it.onLinkState(Constants.LINK_FAIL) }
                isExecuting = false
                mDataByteList.clear()
                inputStream?.close()
                outputStream?.close()
                socket?.close()
                dataQueue.clear()
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
                        if (isSuccess) {
                            if (mIsSaveData2File && flightFw != null) {
                                saveData2FileFun(flightFw, source)
                            }
                        } else {
                            it.clear()
                        }
                    }
                }
                CommandType.RealData -> {
                    realDataDeque?.let {
                        val isSuccess = it.offer(source)
                        if (isSuccess) {
                            if (mIsSaveData2File && realFw != null) {
                                saveData2FileFun(realFw, source)
                            }
                        } else {
                            it.clear()
                        }
                    }
                }
                CommandType.SendPulse -> {
                    pulseDataDeque?.let {
                        val isSuccess = it.offer(source)
                        if (isSuccess) {
                            if (mIsSaveData2File && pulseFw != null) {
                                saveData2FileFun(pulseFw, source)
                            }
                        } else {
                            it.clear()
                        }
                    }
                }
                CommandType.ReadYcData -> {
                    if (mIsSaveData2File && ycFw != null) {
                        saveData2FileFun(ycFw, source)
                    }
                    bytesCallbackMap[commandType]?.forEach {
                        it.onData(source)
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

    private fun saveData2FileFun(fw: FileWriter?, source: ByteArray) {
        if (fw != null) {
            val time = System.currentTimeMillis().toString()
            val hexStr = source.toHexString(false)
            val sb = StringBuilder(time).append(" ").append(hexStr).append("\n")
            fw.write(sb.toString())
            fw.flush()
        }
    }

    /**
     * 销毁socket
     */
    private fun destroy() {
        try {
            host = null
            linkedDeviceSerialNo = null
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
    fun release() {
        isExecuting = true
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
    fun initLink(SerialNo: String?, ip: String?) {
        cleanAllData()
        linkedDeviceSerialNo = SerialNo
        host = ip
        isExecuting = true
        realDataDeque = ArrayBlockingQueue<ByteArray>(50)
        flightDeque = ArrayBlockingQueue<ByteArray>(50)
        pulseDataDeque = ArrayBlockingQueue<ByteArray>(50)

        mRequestExecutor = Executors.newSingleThreadExecutor()
        future = mRequestExecutor?.submit(requestRunnable)

        mQueueExecutor = Executors.newSingleThreadExecutor()
        mQueueFuture = mQueueExecutor?.submit {
            try {
                while (isExecuting) {
                    val dataList = dataQueue.take()
                    dataList?.let {
                        if (isLegalBytes(it)) {
                            mDataByteList.clear()
                        }
                        mDataByteList.addAll(Bytes.asList(*it))
                        dealByteList()
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

    private fun isLegalBytes(bytes: ByteArray): Boolean {
        if (bytes.size > 1 && bytes[0].toInt() == DEVICE_NO) {
            val commandType: CommandType? =
                CommandType.values().firstOrNull { it.funCode == bytes[1] }
            if (commandType != null) {
                return true
            }
        }
        return false
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
                    Log.d("zhangan",
                        "send data " + ByteUtil.bytes2HexStr(data).chunked(2).joinToString(" "))
                } else {
                    Log.d("zhangan", "发送失败")
                    emitter.onNext(false)
                }
            } catch (e: Exception) {
                e.printStackTrace()
//                emitter.onError(e)
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
//                emitter.onError(e)
            } finally {
                emitter.onComplete()
            }
        }.repeatWhen { objectObservable: Observable<Any?> ->
            objectObservable.delay(time, unit)
        }.subscribeOn(Schedulers.io()).subscribe()
    }

}