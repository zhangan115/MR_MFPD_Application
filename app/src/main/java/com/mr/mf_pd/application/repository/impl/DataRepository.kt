package com.mr.mf_pd.application.repository.impl

import com.mr.mf_pd.application.common.CheckType
import io.reactivex.disposables.Disposable

interface DataRepository {

    /**
     * 设置检测类型
     */
    fun setCheckType(checkType: CheckType)

    /**
     * 获取检测类型
     */
    fun getCheckType(): CheckType

    /**
     * 切换实时数据监测通道
     */
    fun switchPassageway(passageway: Int, commandType: Int)

    /**
     * 关闭实时数据监测通道
     */
    fun closePassageway()

    /**
     * 一秒读取一次遥测数据
     */
    fun readRepeatData(): Disposable

}