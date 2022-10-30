package com.mr.mf_pd.application.utils

import android.os.Handler
import android.os.HandlerThread
import android.os.Message
import android.text.TextUtils
import android.util.Log
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter

object ZLog {

    var save2File = false
    private var logFilePath: File? = null
    var searchKey = "MR_MFPD_"
    var bufferedWriter: BufferedWriter? = null
    const val saveLogCode = 10000
    const val stopSaveLogCode = 10001
    var threadHandler: Handler? = null

    public fun init(save2File: Boolean, path: File?) {
        this.save2File = save2File
        this.logFilePath = path
        if (save2File && path != null && path.exists()) {
            val logFile = File(path,
                DateUtil.timeFormat(System.currentTimeMillis(), "yyyy_MM_dd_HH_mm_ss") + ".log")
            if (logFile.exists()) {
                logFile.delete()
            }
            if (!logFile.exists()) {
                logFile.createNewFile()
            }
            if (logFilePath != null && logFile.exists() && logFile.canWrite()) {
                bufferedWriter = BufferedWriter(FileWriter(logFile))
                val handlerThread = HandlerThread("ZLog")
                handlerThread.start()
                threadHandler = object : Handler(handlerThread.looper) {
                    override fun handleMessage(msg: Message) {
                        super.handleMessage(msg)
                        if (msg.what == saveLogCode) {
                            val content: String = msg.obj as String
                            bufferedWriter?.let {
                                it.write(content)
                                it.newLine()
                                it.flush()
                            }
                        } else if (msg.what == stopSaveLogCode) {
                            handlerThread.quitSafely()
                            try {
                                bufferedWriter?.close()
                                bufferedWriter = null
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                }
            }
        }
    }


    public fun d(tag: String, content: String?) {
        this.d(tag, content, true)
    }

    public fun d(
        tag: String,
        content: String?,
        showLog: Boolean = true,
    ) {
        if (!TextUtils.isEmpty(content)) {
            if (showLog) {
                Log.d(searchKey + tag, content!!)
            }
            if (save2File) {
                saveLog2File(searchKey + tag + "_d" + "\t\t\t" + content)
            }
        }
    }

    public fun e(
        tag: String,
        content: String?,
        showLog: Boolean = true,
    ) {
        if (!TextUtils.isEmpty(content)) {
            if (showLog) {
                Log.e(searchKey + tag, content!!)
            }
            if (save2File) {
                saveLog2File(searchKey + tag + "_e" + "\t\t\t" + content)
            }
        }
    }

    public fun i(
        tag: String,
        content: String?,
        showLog: Boolean = true,
    ) {
        if (!TextUtils.isEmpty(content)) {
            if (showLog) {
                Log.i(searchKey + tag, content!!)
            }
            if (save2File) {
                saveLog2File(searchKey + tag + "_i" + "\t\t\t" + content)
            }
        }
    }

    public fun saveLog2File(saveContent: String) {
        if (TextUtils.isEmpty(saveContent)) {
            return
        }
        val time = DateUtil.timeFormat(System.currentTimeMillis(), null)
        val sb = StringBuilder(time).append("\t\t").append(saveContent)
        val message = Message.obtain()
        message.what = saveLogCode
        message.obj = sb.toString()
        threadHandler?.sendMessage(message)
    }

    fun stopSaveLog() {
        save2File = false
        threadHandler?.sendEmptyMessage(stopSaveLogCode)
    }
}