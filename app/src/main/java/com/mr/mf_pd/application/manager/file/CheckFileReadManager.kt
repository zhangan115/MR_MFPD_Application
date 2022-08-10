package com.mr.mf_pd.application.manager.file

import android.text.TextUtils
import android.util.Log
import androidx.annotation.MainThread
import com.mr.mf_pd.application.common.ConstantStr
import com.mr.mf_pd.application.manager.socket.callback.BytesDataCallback
import com.mr.mf_pd.application.manager.socket.comand.CommandType
import com.mr.mf_pd.application.utils.ByteUtil
import com.mr.mf_pd.application.view.file.model.CheckConfigModel
import io.reactivex.disposables.Disposable
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.util.*
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future

class CheckFileReadManager {

    private var realFR: FileReader? = null
    private var ycFR: FileReader? = null
    private var flightFR: FileReader? = null
    private var pulseFR: FileReader? = null
    private var fdFR: FileReader? = null

    private var ycDisposable: Disposable? = null
    private var realDisposable: Disposable? = null
    private var pulseDisposable: Disposable? = null
    private var flightDisposable: Disposable? = null
    private var fdDisposable: Disposable? = null

    var realDataDeque: ArrayBlockingQueue<ByteArray>? = null//实时数据队列
    var flightDeque: ArrayBlockingQueue<ByteArray>? = null//飞行数据队列
    var pulseDataDeque: ArrayBlockingQueue<ByteArray>? = null//脉冲数据队列

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

    var settingFile: File? = null
    var checkConfigFile: File? = null

    fun initQueue() {
        realDataDeque = ArrayBlockingQueue<ByteArray>(50)
        flightDeque = ArrayBlockingQueue<ByteArray>(50)
        pulseDataDeque = ArrayBlockingQueue<ByteArray>(50)
    }

    fun setFile(checkFile: File) {
        this.checkFile = checkFile
        this.settingFile = File(checkFile, ConstantStr.CHECK_FILE_SETTING)
        this.checkConfigFile = File(checkFile, ConstantStr.CHECK_FILE_CONFIG)
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
            return Executors.newFixedThreadPool(5)
        }
    //文件的保存信息
    var config:CheckConfigModel?=null

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
            if (ycFile.exists()) {
                ycFR = FileReader(ycFile)
            }

            val realFile = File(file, ConstantStr.CHECK_REAL_DATA)
            if (realFile.exists()) {
                realFR = FileReader(realFile)
            }

            val flightFile = File(file, ConstantStr.CHECK_FLIGHT_FILE_NAME)
            if (flightFile.exists()) {
                flightFR = FileReader(flightFile)
            }

            val pulseFile = File(file, ConstantStr.CHECK_PULSE_FILE_NAME)
            if (pulseFile.exists()) {
                pulseFR = FileReader(pulseFile)
            }
            val fdFile = File(file, ConstantStr.CHECK_FD_FILE_NAME)
            if (fdFile.exists()) {
                fdFR = FileReader(fdFile)
            }

        }

        ycFuture = executorService.submit {
            val startTime = System.currentTimeMillis()
            ycFR?.let { fr ->
                readDataFromFr(fr, null,CommandType.ReadYcData)
                Log.d("zhangan", "yc time is ${System.currentTimeMillis() - startTime}")
            }
        }
        realFuture = executorService.submit {
            val startTime = System.currentTimeMillis()
            realFR?.let { fr ->
                readDataFromFr(fr, realDataDeque)
                Log.d("zhangan", "real time is ${System.currentTimeMillis() - startTime}")
            }
        }
        flightFuture = executorService.submit {
            val startTime = System.currentTimeMillis()
            flightFR?.let { fr ->
                readDataFromFr(fr, flightDeque)
                Log.d("zhangan", "flight time is ${System.currentTimeMillis() - startTime}")
            }
        }
        pulseFuture = executorService.submit {
            val startTime = System.currentTimeMillis()
            pulseFR?.let { fr ->
                readDataFromFr(fr, pulseDataDeque)
                Log.d("zhangan", "pulse time is ${System.currentTimeMillis() - startTime}")
            }
        }
        fdFuture = executorService.submit {
            val startTime = System.currentTimeMillis()
            fdFR?.let { fr ->
                readDataFromFr(fr, null,CommandType.FdData)
                Log.d("zhangan", "fd time is ${System.currentTimeMillis() - startTime}")
            }
        }
    }

    private fun readDataFromFr(
        fr: FileReader,
        dataDeque: ArrayBlockingQueue<ByteArray>?,
        type: CommandType? = null,
    ) {
        val br = BufferedReader(fr)
        var result: String?
        var lastTime: Long? = null
        while (br.readLine().also { result = it } != null) {
            try {
                if (!TextUtils.isEmpty(result)) {
                    val list = result!!.split(" ")
                    if (list.size == 2) {
                        val time = list.first().toLongOrNull()
                        time?.let {
                            val source = ByteUtil.hexStr2bytes(list[1])
                            if (source != null) {
                                when (type) {
                                    CommandType.ReadYcData -> {
                                        if (lastTime != null) {
                                            Thread.sleep(it - lastTime!!)
                                        }
                                        getCallback(type).forEach { callback ->
                                            callback.onData(source)
                                        }
                                    }
                                    CommandType.FdData -> {
                                        if (lastTime == null) {
                                            lastTime = config?.beginTime
                                        }
                                        if (lastTime != null) {
                                            Thread.sleep(it - lastTime!!)
                                        }
                                        getCallback(type).forEach { callback ->
                                            callback.onData(source)
                                        }
                                    }
                                    else -> {
                                        if (lastTime != null) {
                                            Thread.sleep(it - lastTime!!)
                                        }
                                        dataDeque?.offer(source)
                                    }
                                }
                                lastTime = time
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private var ycFuture: Future<*>? = null
    private var realFuture: Future<*>? = null
    private var flightFuture: Future<*>? = null
    private var pulseFuture: Future<*>? = null
    private var fdFuture: Future<*>? = null


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
            fdDisposable?.dispose()

            ycFR = null
            realFR = null
            fdFR = null
            flightFR = null
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

}