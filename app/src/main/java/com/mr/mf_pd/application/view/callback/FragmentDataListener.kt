package com.mr.mf_pd.application.view.callback

interface FragmentDataListener {
    fun onYcDataChange(bytes: ByteArray)
    fun cleanCurrentData()
    fun isAdd():Boolean
}