package com.mr.mf_pd.application.manager.file

import androidx.annotation.Nullable
import com.google.common.primitives.Bytes
import com.google.gson.Gson
import com.mr.mf_pd.application.common.ConstantStr
import com.mr.mf_pd.application.manager.socket.callback.PulseDataListener
import com.mr.mf_pd.application.manager.socket.callback.ReadListener
import com.mr.mf_pd.application.manager.socket.callback.ReadSettingDataCallback
import com.mr.mf_pd.application.manager.socket.callback.YcDataCallback
import com.mr.mf_pd.application.model.SettingBean
import com.mr.mf_pd.application.repository.callback.ReadDataFromFileCallback
import com.mr.mf_pd.application.utils.FileUtils
import com.sito.tool.library.utils.ByteLibUtil
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.ObservableOnSubscribe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.io.File
import java.io.InputStream
import java.util.concurrent.TimeUnit

class ReadFileManager {

    private var realDataIStream: InputStream? = null
    private var ycDataIStream: InputStream? = null
    private var fdDataIStream: InputStream? = null

    private var settingFile: File? = null
    private var ycFile: File? = null
    private var realDataFile: File? = null
    private var fdFile: File? = null

    private var realDisposable: Disposable? = null
    private var ycDisposable: Disposable? = null
    private var fdDisposable: Disposable? = null

    var readDataSize = 0
    var readYcDataSize = 0
    var readFdDataSize = 0

    private var readListener: ReadListener? = null
    private val mPulseDataListener: PulseDataListener? = null

    var realDataFromFileCallback: ReadDataFromFileCallback? = null
    var readSettingCallbacks: ReadSettingDataCallback? = null
    var ycDataCallback: YcDataCallback? = null


    companion object {

        private var instance: ReadFileManager? = null
            get() {
                if (field == null) {
                    field = ReadFileManager()
                }
                return field
            }

        fun get(): ReadFileManager {
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
        if (settingFile != null && settingFile!!.exists()) {
            val str = FileUtils.readStrFromFile(settingFile)
            val settingBean = Gson().fromJson(str, SettingBean::class.java)
            realDataFromFileCallback?.onSettingData(settingBean)
        }
    }

    /**
     * 读取设置参数
     */
    @Nullable
    fun readSettingData(): ByteArray? {
        if (settingFile != null && settingFile!!.exists()) {
            return Bytes.toArray(FileUtils.readBytesFromFile(settingFile!!))
        }
        return null
    }

    /**
     * 开始读取数据
     */
    fun startReadReadData() {
        if (checkFile == null) {
            throw RuntimeException("请配置好读取文件")
        }
        releaseReadFile()
        readRealFile()
        readYcFile()
    }

    private fun readRealFile() {
        realDisposable =
            Observable.create(ObservableOnSubscribe { emitter: ObservableEmitter<ByteArray> ->
                try {
                    if (realDataIStream != null) {
                        val buf = ByteArray(1024)
                        val size = realDataIStream!!.read(buf, readDataSize, buf.size)
                        if (size != -1) {
                            val lengthBytes = byteArrayOf(0x00, 0x00, buf[3], buf[4])
                            val length = ByteLibUtil.getInt(lengthBytes) * 6 + 7
                            val source = Bytes.toArray(Bytes.asList(*buf).subList(0, length))
                            readDataSize += length
                            emitter.onNext(source)
                        }
                    } else {
                        emitter.onComplete()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    emitter.onError(e)
                }
            } as ObservableOnSubscribe<ByteArray>)
                .timeout(20, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ bytes: ByteArray? ->
                    if (bytes != null) {
                        readListener?.onData(bytes)
                    }
                }) { obj: Throwable -> obj.printStackTrace() }
    }

    private fun readYcFile() {
        ycDisposable =
            Observable.create(ObservableOnSubscribe { emitter: ObservableEmitter<ByteArray> ->
                try {
                    if (ycDataIStream != null) {
                        val buf = ByteArray(1024)
                        val size = ycDataIStream!!.read(buf, readYcDataSize, buf.size)
                        if (size != -1) {
                            val lengthBytes = byteArrayOf(0x00, 0x00, buf[3], buf[4])
                            val length = ByteLibUtil.getInt(lengthBytes) * 6 + 7
                            val source = Bytes.toArray(Bytes.asList(*buf).subList(0, length))
                            readYcDataSize += length
                            emitter.onNext(source)
                        }
                    } else {
                        emitter.onComplete()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    emitter.onError(e)
                }
            } as ObservableOnSubscribe<ByteArray>)
                .timeout(1, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ bytes: ByteArray? -> realDataFromFileCallback?.onYcData(bytes) }) { obj: Throwable -> obj.printStackTrace() }
    }

    fun releaseReadFile() {
        readDataSize = 0
        readYcDataSize = 0
        readFdDataSize = 0

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


}