package com.mr.mf_pd.application.repository.callback

/**
 * 实时数据回调
 */
interface RealDataCallback {
    /**
     * 实时数据变化回调
     */
    fun onRealDataChanged()

    /**
     * 接收数据回调
     */
    fun onReceiverValueChange()
}