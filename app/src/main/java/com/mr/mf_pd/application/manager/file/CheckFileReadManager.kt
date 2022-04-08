package com.mr.mf_pd.application.manager.file

import android.util.Log
import androidx.annotation.MainThread
import com.google.common.primitives.Bytes
import com.mr.mf_pd.application.common.ConstantStr
import com.mr.mf_pd.application.manager.socket.callback.BytesDataCallback
import com.mr.mf_pd.application.manager.socket.comand.CommandType
import com.sito.tool.library.utils.ByteLibUtil
import io.reactivex.disposables.Disposable
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.util.*
import java.util.concurrent.ArrayBlockingQueue
import kotlin.collections.ArrayList

class CheckFileReadManager {

    private var realDataIStream: InputStream? = null
    private var ycDataIStream: InputStream? = null
    private var fdDataIStream: InputStream? = null
    private var flightDataIStream: InputStream? = null

    var settingFile: File? = null
    private var ycFile: File? = null
    private var realDataFile: File? = null
    private var fdFile: File? = null
    private var flightDataFile: File? = null

    private var realDisposable: Disposable? = null
    private var ycDisposable: Disposable? = null
    private var fdDisposable: Disposable? = null
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
    var fdDataDeque: ArrayBlockingQueue<ByteArray>? = null//放电数据队列
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

    fun initQueue() {
        fdDataDeque = ArrayBlockingQueue<ByteArray>(50)
        realDataDeque = ArrayBlockingQueue<ByteArray>(50)
        flightDeque = ArrayBlockingQueue<ByteArray>(50)
    }

    fun setFile(checkFile: File) {
        this.checkFile = checkFile
        val files = checkFile.listFiles()
        files?.forEach {
            if (it.name.equals(ConstantStr.CHECK_YC_FILE_NAME)) {
                ycFile = it
            }
            if (it.name.equals(ConstantStr.CHECK_FILE_SETTING)) {
                settingFile = it
            }
            if (it.name.equals(ConstantStr.CHECK_REAL_DATA)) {
                realDataFile = it
            }
            if (it.name.equals(ConstantStr.CHECK_FLIGHT_FILE_NAME)) {
                flightDataFile = it
            }
        }
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
        ycFile?.let {
            ycDataIStream = FileInputStream(it)
        }
        realDataFile?.let {
            realDataIStream = FileInputStream(it)
        }
        flightDataFile?.let {
            flightDataIStream = FileInputStream(it)
        }
        if (ycDataIStream != null) {
            startReadYcDataFromFile()
        }
        if (realDataIStream != null) {
            startReadRealDataFromFile()
        }
        if (flightDataIStream != null) {
            startReadFlightDataFromFile()
        }
    }

    private fun startReadYcDataFromFile() {
        readYcData.clear()
        ycBytePosition = 0
        surplusYcData.clear()
        readYcDataFromFile()
    }

    fun readYcDataFromFile() {
        if (ycDataIStream != null) {
            if (readYcData.size > ycBytePosition) {
                getCallback(CommandType.ReadYcData).forEach {
                    it.onData(Bytes.toArray(readYcData[ycBytePosition]))
                }
            } else {
                readYcFile()
            }
            ycBytePosition++
        }
    }

    private fun readYcFile() {
        if (ycDataIStream != null) {
            val buf = ByteArray(1024 * 4)
            val size = ycDataIStream!!.read(buf, 0, buf.size)
            if (size != -1) {
                val currentBytes = ArrayList<Byte>()
                if (surplusYcData.isNotEmpty()) {
                    currentBytes.addAll(surplusYcData)
                    surplusYcData.clear()
                }
                currentBytes.addAll(Bytes.asList(*buf).subList(0, size))
                var currentPosition = 0
                var length: Int
                while (currentPosition < currentBytes.size) {
                    if (currentBytes[currentPosition].toInt() == 1 && currentBytes.size > currentPosition + 3 && currentBytes[currentPosition + 1].toInt() == 3) {
                        length = currentBytes[currentPosition + 2].toInt() * 4 + 5
                        if (currentBytes.size < currentPosition + length) {
                            surplusYcData.addAll(currentBytes)
                            break
                        } else {
                            val list =
                                ArrayList<Byte>(currentBytes.subList(currentPosition,
                                    currentPosition + length))
                            readYcData.add(list)
                            currentPosition += length
                        }
                    } else {
                        surplusYcData.addAll(currentBytes)
                        break
                    }
                }
            }
            if (readYcData.size > ycBytePosition) {
                getCallback(CommandType.ReadYcData).forEach {
                    it.onData(Bytes.toArray(readYcData[ycBytePosition]))
                }
            } else {
                getCallback(CommandType.ReadYcData).forEach {
                    it.onData(ByteArray(0))
                }
            }
        }
    }

    private fun commandCallback(commandType: CommandType?, source: ByteArray) {
        if (commandType != null) {
            when (commandType) {
                CommandType.FlightValue -> {
                    val isSuccess = flightDeque?.offer(source)
                }
                CommandType.RealData -> {
                    val isSuccess = realDataDeque?.offer(source)
                }
                CommandType.FdData -> {
                    fdDataDeque?.offer(source)
                }
                else -> {
                    bytesCallbackMap[commandType]?.forEach {
                        it.onData(source)
                    }
                }
            }
        }
    }

    private fun startReadRealDataFromFile() {
        readRealData.clear()
        realBytePosition = 0
        surplusRealData.clear()
        readRealDataFromFile()
    }

    fun readRealDataFromFile() {
        if (realDataIStream != null) {
            if (readRealData.size > realBytePosition) {
                commandCallback(CommandType.RealData, Bytes.toArray(readRealData[realBytePosition]))
            } else {
                readRealDataFile()
            }
            realBytePosition++
        }
    }

    private fun readRealDataFile() {
        if (realDataIStream != null) {
            val buf = ByteArray(1024 * 4)
            val size = realDataIStream!!.read(buf, 0, buf.size)
            if (size != -1) {
                val currentBytes = ArrayList<Byte>()
                if (surplusRealData.isNotEmpty()) {
                    currentBytes.addAll(surplusRealData)
                    surplusRealData.clear()
                }
                currentBytes.addAll(Bytes.asList(*buf).subList(0, size))
                var currentPosition = 0
                var length: Int
                while (currentPosition < currentBytes.size) {
                    if (currentBytes[currentPosition].toInt() == 1 && currentBytes.size > currentPosition + 7 && currentBytes[currentPosition + 1].toInt() == 8) {
                        val lengthBytes = byteArrayOf(0x00,
                            0x00,
                            currentBytes[3 + currentPosition],
                            currentBytes[4 + currentPosition])
                        length = ByteLibUtil.getInt(lengthBytes) * 6 + 7
                        if (currentBytes.size < currentPosition + length) {
                            surplusRealData.addAll(currentBytes)
                            break
                        } else {
                            val list = Vector(currentBytes.subList(currentPosition,
                                currentPosition + length))
                            readRealData.add(list)
                            currentPosition += length
                        }
                    } else {
                        surplusRealData.addAll(currentBytes.subList(currentPosition,
                            currentBytes.size))
                        break
                    }
                }
            }
            if (readRealData.size > realBytePosition) {
                commandCallback(CommandType.RealData, Bytes.toArray(readRealData[realBytePosition]))
            } else {
                getCallback(CommandType.RealData).forEach {
                    it.onData(ByteArray(0))
                }
                commandCallback(CommandType.RealData, ByteArray(0))
            }
        }
    }

    private fun startReadFlightDataFromFile() {
        readFlightData.clear()
        flightBytePosition = 0
        surplusFlightData.clear()
        readFlightDataFromFile()
    }

    fun readFlightDataFromFile() {
        if (flightDataIStream != null) {
            if (readFlightData.size > flightBytePosition) {
                commandCallback(CommandType.FlightValue,
                    Bytes.toArray(readFlightData[flightBytePosition]))
            } else {
                readFlightDataFile()
            }
            flightBytePosition++
        }
    }

    private fun readFlightDataFile() {
        if (flightDataIStream != null) {
            val buf = ByteArray(1024 * 4)
            val size = flightDataIStream!!.read(buf, 0, buf.size)
            if (size != -1) {
                val currentBytes = ArrayList<Byte>()
                if (surplusFlightData.isNotEmpty()) {
                    currentBytes.addAll(surplusFlightData)
                    surplusFlightData.clear()
                }
                currentBytes.addAll(Bytes.asList(*buf).subList(0, size))
                var currentPosition = 0
                var length: Int
                while (currentPosition < currentBytes.size) {
                    if (currentBytes[currentPosition].toInt() == 1 && currentBytes.size > currentPosition + 7 && currentBytes[currentPosition + 1].toInt() == 8) {
                        val lengthBytes = byteArrayOf(0x00,
                            0x00,
                            currentBytes[3 + currentPosition],
                            currentBytes[4 + currentPosition])
                        length = ByteLibUtil.getInt(lengthBytes) * 6 + 7
                        if (currentBytes.size < currentPosition + length) {
                            surplusFlightData.addAll(currentBytes)
                            break
                        } else {
                            val list = ArrayList(currentBytes.subList(currentPosition,
                                currentPosition + length))
                            readFlightData.add(list)
                            currentPosition += length
                        }
                    } else {
                        surplusFlightData.addAll(currentBytes.subList(currentPosition,
                            currentBytes.size))
                        break
                    }
                }
            }
            if (readFlightData.size > flightBytePosition) {
                commandCallback(CommandType.FlightValue,
                    Bytes.toArray(readFlightData[flightBytePosition]))
            } else {
                commandCallback(CommandType.FlightValue, ByteArray(0))
            }
        }
    }

    fun releaseReadFile() {

        realDataIStream?.close()
        ycDataIStream?.close()
        fdDataIStream?.close()
        flightDataIStream?.close()

        realDisposable?.dispose()
        ycDisposable?.dispose()
        fdDisposable?.dispose()
        flightDisposable?.dispose()

        ycDataIStream = null
        realDataIStream = null
        fdDataIStream = null
        flightDataIStream = null
    }

}