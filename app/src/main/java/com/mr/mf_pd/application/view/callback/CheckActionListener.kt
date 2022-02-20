package com.mr.mf_pd.application.view.callback

/***
 * 操作回调监听
 * @author zhangan
 * @since 2022-02-20
 */
interface CheckActionListener {
    /**
     * 增加通道门限值
     */
    fun addLimitValue()

    /**
     * 降低通道门限值
     */
    fun downLimitValue()

    /**
     * 获取设置数据
     */
    fun getSettingValues(): List<Float>

    /**
     * 写入设置
     */
    fun writeSettingValue()

    /**
     * 通道门限值位置
     */
    fun getLimitPosition(): Int

    /**
     * 频带检测位置
     */
    fun getBandDetectionPosition(): Int

    /**
     * 修改频带检测类型
     */
    fun changeBandDetectionModel()
}

