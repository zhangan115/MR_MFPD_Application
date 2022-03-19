package com.mr.mf_pd.application.view.callback

/**
 * 数据变化监听
 * @author anson
 * @since 2022-03-19
 */
interface FragmentDataListener {
    /**
     * 遥测数据发生变化
     */
    fun onYcDataChange(bytes: ByteArray)

    /**
     * 清理数据
     */
    fun cleanCurrentData()

    /**
     * 判断是否添加
     */
    fun isAdd(): Boolean
}