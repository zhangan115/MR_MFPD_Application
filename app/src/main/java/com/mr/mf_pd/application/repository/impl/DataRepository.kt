package com.mr.mf_pd.application.repository.impl

import androidx.lifecycle.MutableLiveData
import com.mr.mf_pd.application.common.CheckType
import com.mr.mf_pd.application.manager.socket.callback.BytesDataCallback
import com.mr.mf_pd.application.repository.callback.DataCallback
import com.mr.mf_pd.application.repository.callback.RealDataCallback
import io.reactivex.disposables.Disposable
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

interface DataRepository {

    /**
     * 获取图表数据
     */
    fun getPhaseData(chartType: Int): ArrayList<HashMap<Int, Float?>>

    /**
     * 设置实时数据的回调
     * @param callback 回调
     */
    fun addRealDataCallback(callback: RealDataCallback)

    /**
     * 增加遥测数据的回调
     */
    fun addYcDataCallback(callback: BytesDataCallback)

    /**
     * 移除遥测数据回调
     */
    fun removeYcDataCallback(callback: BytesDataCallback)

    /**
     * 切换实时数据监测通道
     */
    fun switchPassageway(passageway: Int,commandType: Int)


    fun addHufData(callback: DataCallback)

    /**
     * 清理数据
     */
    fun cleanData()

    /**
     * 关闭实时数据监测通道
     */
    fun closePassageway()

    /**
     * 设置检测类型
     */
    fun setCheckType(checkType: CheckType)

    /**
     * 获取检测类型
     */
    fun getCheckType(): CheckType

    /**
     * 获取线性图表数据
     */
    fun getGainValueList(): MutableLiveData<Vector<Float>>

    /**
     * 读取遥测信息
     */
    fun readYcValue(): Disposable

    /**
     * 一秒读取一次遥测数据
     */
    fun readRepeatData(): Disposable

}