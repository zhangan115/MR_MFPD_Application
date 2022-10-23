package com.mr.mf_pd.application.utils

import android.text.TextUtils
import android.util.Log

object ZLog {

    var save2File = false
    var logFilePath = ""
    var searchKey = ""

    public fun init(save2File: Boolean, path: String) {
        this.save2File = save2File
        this.logFilePath = path
    }

    public fun d(tag: String, content: String?) {
        if (!TextUtils.isEmpty(content)) {
            Log.d(tag, content!!)
            if (save2File) {
                saveLog2File(searchKey + "_" + tag + "_d_" + "    " + content)
            }
        }
    }

    public fun e(tag: String, content: String?) {
        if (!TextUtils.isEmpty(content)) {
            Log.e(tag, content!!)
            if (save2File) {
                saveLog2File(searchKey + "_" + tag + "_e_" + "    " + content)
            }
        }
    }

    public fun i(tag: String, content: String?) {
        if (!TextUtils.isEmpty(content)) {
            Log.i(tag, content!!)
            if (save2File) {
                saveLog2File(searchKey + "_" + tag + "_i_" + "    " + content)
            }
        }
    }

    public fun saveLog2File(saveContent: String) {
        if (TextUtils.isEmpty(saveContent)){
            return
        }

    }
}