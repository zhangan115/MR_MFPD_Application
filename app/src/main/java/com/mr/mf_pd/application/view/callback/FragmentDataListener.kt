package com.mr.mf_pd.application.view.callback

/**
 * 数据变化监听
 * @author anson
 * @update 更新一秒刷新一次页面
 * @since 2022-03-19
 */
interface FragmentDataListener {

    /**
     * 一秒更新一次页面事件
     */
    fun onOneSecondUiChange()

    /**
     * 遥测数据发生变化
     */
    fun onYcDataChange(bytes: ByteArray)

    /**
     * 通道门限值发生变化
     */
    fun onLimitValueChange(value: Int)

    /**
     * 清理数据
     */
    fun cleanCurrentData()

    /**
     * 判断是否添加
     */
    fun isAdd(): Boolean
}