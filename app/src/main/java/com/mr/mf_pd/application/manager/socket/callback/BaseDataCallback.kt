package com.mr.mf_pd.application.manager.socket.callback

interface BaseDataCallback {
    fun onData(source: ByteArray)
}