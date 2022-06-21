package com.mr.mf_pd.application.manager.file

import android.util.Log
import androidx.annotation.MainThread
import com.mr.mf_pd.application.common.ConstantStr
import com.mr.mf_pd.application.manager.socket.callback.BytesDataCallback
import com.mr.mf_pd.application.manager.socket.comand.CommandType
import com.mr.mf_pd.application.utils.ByteUtil
import com.mr.mf_pd.application.utils.DateUtil
import io.reactivex.disposables.Disposable
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.lang.Exception
import java.util.*
import java.util.concurrent.*
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

    private val executorService: ExecutorService
        get() {
            return Executors.newFixedThreadPool(4)
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

        ycFuture = executorService.submit {
            Log.d("zhangan",
                "ycFuture startTime" + DateUtil.timeFormat(System.currentTimeMillis(), null))
            ycFR?.let { fr ->
                readDataFromFr(fr, ycDataDeque)
            }

        }
        realFuture = executorService.submit {
            Log.d("zhangan",
                "realFuture startTime" + DateUtil.timeFormat(System.currentTimeMillis(), null))
            realFR?.let { fr ->
                readDataFromFr(fr, realDataDeque)
            }
        }
        flightFuture = executorService.submit {
            Log.d("zhangan",
                "flightFuture startTime" + DateUtil.timeFormat(System.currentTimeMillis(), null))
            flightFR?.let { fr ->
                readDataFromFr(fr, flightDeque)
            }
        }
        pulseFuture = executorService.submit {
            Log.d("zhangan",
                "pulseFuture startTime" + DateUtil.timeFormat(System.currentTimeMillis(), null))
            pulseFR?.let { fr ->
                readDataFromFr(fr, pulseDataDeque)
            }
        }
    }

    private fun readDataFromFr(fr: FileReader, dataDeque: ArrayBlockingQueue<ByteArray>?) {
        val br = BufferedReader(fr)
        var result: String? = null
        var lastTime: Long? = null
        while (br.readLine().also { result = it } != null) {
            try {
                val source = result?.toByteArray()
                if (source != null && source.size > 8) {
                    val timeBytes = ByteArray(8)
                    val dataBytes = ByteArray(source.size - 8)
                    System.arraycopy(timeBytes, 0, source, 0, timeBytes.size)
                    System.arraycopy(dataBytes, 0, source, timeBytes.size, dataBytes.size)
                    val time = ByteUtil.byteToLong(timeBytes)
                    if (lastTime != null) {
                        time - lastTime
                        Thread.sleep(time)
                    }
                    dataDeque?.put(dataBytes)
                    lastTime = time
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    var ycFuture: Future<*>? = null
    var realFuture: Future<*>? = null
    var flightFuture: Future<*>? = null
    var pulseFuture: Future<*>? = null


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
        try {
            realFR?.close()
            ycFR?.close()
            fdFR?.close()
            flightFR?.close()

            executorService.shutdown()
            ycFuture?.cancel(true)
            realFuture?.cancel(true)
            flightFuture?.cancel(true)
            pulseFuture?.cancel(true)

            realDisposable?.dispose()
            ycDisposable?.dispose()
            pulseDisposable?.dispose()
            flightDisposable?.dispose()

            ycFR = null
            realFR = null
            fdFR = null
            flightFR = null
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

}