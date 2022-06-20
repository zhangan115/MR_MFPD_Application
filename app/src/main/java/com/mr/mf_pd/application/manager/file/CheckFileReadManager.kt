package com.mr.mf_pd.application.manager.file

import androidx.annotation.MainThread
import com.mr.mf_pd.application.common.ConstantStr
import com.mr.mf_pd.application.manager.socket.callback.BytesDataCallback
import com.mr.mf_pd.application.manager.socket.comand.CommandType
import io.reactivex.disposables.Disposable
import java.io.File
import java.io.FileReader
import java.lang.Exception
import java.util.*
import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicInteger
import kotlin.collections.ArrayList

class CheckFileReadManager {

    private var realFR: FileReader? = null
    private var ycFR: FileReader? = null
    private var fdFR: FileReader? = null
    private var flightFR: FileReader? = null
    private var pulseFR: FileReader? = null

    private var realDataFile: File? = null

    private var ycDisposable: Disposable? = null
    private var realDisposable: Disposable? = null
    private var pulseDisposable: Disposable? = null
    private var flightDisposable: Disposable? = null

    private var realBytePosition = 0
    private val readRealData = Vector<Vector<Byte>>() //读取出来的遥测数据
    private val surplusRealData = Vector<Byte>() //读取处理的未处理的不完整遥测数据

    private var ycBytePosition = 0
    private val readYcData = ArrayList<ArrayList<Byte>>() //读取出来的遥测数据
    private val surplusYcData = ArrayList<Byte>() //读取处理的未处理的不完整遥测数据

    private var flightBytePosition = 0
    private val readFlightData = ArrayList<ArrayList<Byte>>() //读取出来的飞行数据
    private val surplusFlightData = ArrayList<Byte>() //读取处理的未处理的不完整飞行数据

    var realDataDeque: ArrayBlockingQueue<ByteArray>? = null//实时数据队列
    var flightDeque: ArrayBlockingQueue<ByteArray>? = null//飞行数据队列
    var pulseDataDeque: ArrayBlockingQueue<ByteArray>? = null//脉冲数据队列
    var ycDataDeque: ArrayBlockingQueue<ByteArray>? = null//遥测数据

    private val bytesCallbackMap: LinkedHashMap<CommandType, LinkedList<BytesDataCallback>> =
        LinkedHashMap()

    companion object {
        private var instance: CheckFileReadManager? = null
            get() {
                if (field == null) {
                    field = CheckFileReadManager()
                }
                return field
            }

        fun get(): CheckFileReadManager {
            return instance!!
        }
    }

    var checkFile: File? = null

    fun initQueue() {
        ycDataDeque = ArrayBlockingQueue<ByteArray>(50)
        realDataDeque = ArrayBlockingQueue<ByteArray>(50)
        flightDeque = ArrayBlockingQueue<ByteArray>(50)
        pulseDataDeque = ArrayBlockingQueue<ByteArray>(50)
    }

    fun setFile(checkFile: File) {
        this.checkFile = checkFile
    }

    private fun getCallback(commandType: CommandType): LinkedList<BytesDataCallback> {
        val list = LinkedList<BytesDataCallback>()
        if (bytesCallbackMap.containsKey(commandType)) {
            bytesCallbackMap[commandType]?.let { list.addAll(it) }
        }
        return list
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
     * 开始读取数据
     */
    fun startReadData() {
        if (checkFile == null) {
            throw RuntimeException("请配置好读取文件")
        }
        releaseReadFile()
        checkFile?.let { file ->
            val ycFile = File(file, ConstantStr.CHECK_YC_FILE_NAME)
            if (!ycFile.exists()) {
                ycFile.createNewFile()
            }
            ycFR = FileReader(ycFile)

            val realFile = File(file, ConstantStr.CHECK_REAL_DATA)
            if (!realFile.exists()) {
                realFile.createNewFile()
            }
            realFR = FileReader(realFile)

            val flightFile = File(file, ConstantStr.CHECK_FLIGHT_FILE_NAME)
            if (!flightFile.exists()) {
                flightFile.createNewFile()
            }
            flightFR = FileReader(flightFile)

            val pulseFile = File(file, ConstantStr.CHECK_PULSE_FILE_NAME)
            if (!pulseFile.exists()) {
                pulseFile.createNewFile()
            }
            pulseFR = FileReader(pulseFile)
        }
//        ThreadPoolExecutor(4,
//            4,
//            0L,
//            TimeUnit.MINUTES,
//            LinkedBlockingDeque<Runnable>(4),
//            SimpleThreadFactory())
//        val service: ExecutorService = Executors.newFixedThreadPool(4, SimpleThreadFactory())
//        service.submit {
//            Thread.currentThread().id
//        }
    }

    internal class WorkThread(var runnable: Runnable, var counter: AtomicInteger) : Thread() {
        override fun run() {
            super.run()
            try {
                runnable.run()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


//    internal class SimpleThreadFactory() : ThreadFactory {
//        private var counter: AtomicInteger = AtomicInteger(0)
//        override fun newThread(r: Runnable?): Thread {
//            val c = counter.incrementAndGet()
//            return WorkThread(r!!, c)
//        }
//    }

    private fun commandCallback(commandType: CommandType?, source: ByteArray) {
        if (commandType != null) {
            when (commandType) {
                CommandType.FlightValue -> {
                    val isSuccess = flightDeque?.offer(source)
                }
                CommandType.RealData -> {
                    val isSuccess = realDataDeque?.offer(source)
                }
                CommandType.SendPulse -> {
                    pulseDataDeque?.offer(source)
                }
                else -> {
                    bytesCallbackMap[commandType]?.forEach {
                        it.onData(source)
                    }
                }
            }
        }
    }


    fun releaseReadFile() {

        realFR?.close()
        ycFR?.close()
        fdFR?.close()
        flightFR?.close()

        realDisposable?.dispose()
        ycDisposable?.dispose()
        pulseDisposable?.dispose()
        flightDisposable?.dispose()

        ycFR = null
        realFR = null
        fdFR = null
        flightFR = null
    }

}