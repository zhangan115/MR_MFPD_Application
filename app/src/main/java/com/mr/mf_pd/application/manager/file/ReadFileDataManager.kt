package com.mr.mf_pd.application.manager.file

import android.util.Log
import com.google.common.primitives.Bytes
import com.mr.mf_pd.application.common.ConstantStr
import com.mr.mf_pd.application.manager.socket.callback.PulseDataListener
import com.mr.mf_pd.application.manager.socket.callback.ReadListener
import com.mr.mf_pd.application.manager.socket.callback.YcDataCallback
import com.sito.tool.library.utils.ByteLibUtil
import io.reactivex.disposables.Disposable
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.util.*
import kotlin.collections.ArrayList

class ReadFileDataManager {

    private var realDataIStream: InputStream? = null
    private var ycDataIStream: InputStream? = null
    private var fdDataIStream: InputStream? = null

    var settingFile: File? = null
    private var ycFile: File? = null
    private var realDataFile: File? = null
    private var fdFile: File? = null

    private var realDisposable: Disposable? = null
    private var ycDisposable: Disposable? = null
    private var fdDisposable: Disposable? = null

    private val mPulseDataListener: PulseDataListener? = null

    private var realBytePosition = 0
    private val readRealData = Vector<Vector<Byte>>() //读取出来的遥测数据
    private val surplusRealData = Vector<Byte>() //读取处理的未处理的不完整遥测数据

    private var ycBytePosition = 0
    private val readYcData = ArrayList<ArrayList<Byte>>() //读取出来的遥测数据
    private val surplusYcData = ArrayList<Byte>() //读取处理的未处理的不完整遥测数据

    var ycDataCallback: YcDataCallback? = null

    private var readDataCallback: ReadListener? = null

    companion object {

        private var instance: ReadFileDataManager? = null
            get() {
                if (field == null) {
                    field = ReadFileDataManager()
                }
                return field
            }

        fun get(): ReadFileDataManager {
            return instance!!
        }
    }

    var checkFile: File? = null

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
        startReadYcDataFromFile()
        startReadRealDataFromFile()
    }

    private fun startReadYcDataFromFile() {
        readYcData.clear()
        ycBytePosition = 0
        surplusYcData.clear()
        readYcDataFromFile()
    }

    fun readYcDataFromFile() {
        if (readYcData.size > ycBytePosition) {
            ycDataCallback?.onData(Bytes.toArray(readYcData[ycBytePosition]))
        } else {
            readYcFile()
        }
        ycBytePosition++
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
                ycDataCallback?.onData(Bytes.toArray(readYcData[ycBytePosition]))
            } else {
                ycDataCallback?.onData(ByteArray(0))
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
        if (readRealData.size > realBytePosition) {
            readDataCallback?.onData(Bytes.toArray(readRealData[realBytePosition]))
        } else {
            readRealDataFile()
        }
        realBytePosition++
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
                readDataCallback?.onData(Bytes.toArray(readRealData[realBytePosition]))
            } else {
                readDataCallback?.onData(ByteArray(0))
            }
        }
    }

    fun releaseReadFile() {

        realDataIStream?.close()
        ycDataIStream?.close()
        fdDataIStream?.close()

        realDisposable?.dispose()
        ycDisposable?.dispose()
        fdDisposable?.dispose()

        ycDataIStream = null
        realDataIStream = null
        fdDataIStream = null
    }

    /**
     * 增加读取监控
     *
     * @param listener 读取监控
     */
    fun setReadListener(listener: ReadListener?) {
        readDataCallback = listener
    }

}