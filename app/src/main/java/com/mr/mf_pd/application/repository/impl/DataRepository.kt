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
     * 高频与特高频模式下一秒读取一次遥测数据
     */
    fun readRepeatData(): Disposable

    /**
     * 读取连续模式下的100md读取一次遥测数据
     */
    fun readContinuityYcData(): Disposable

}