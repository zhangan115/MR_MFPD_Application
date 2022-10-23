package com.mr.mf_pd.application.manager.udp

interface UDPListener {
    fun onData(byteArray: ByteArray)
    fun onError()
    fun onStart()
}