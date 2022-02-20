package com.mr.mf_pd.application.repository.impl

import androidx.lifecycle.MutableLiveData
import com.mr.mf_pd.application.common.CheckType
import com.mr.mf_pd.application.repository.callback.RealDataCallback
import com.mr.mf_pd.application.view.opengl.`object`.PrPsCubeList
import io.reactivex.disposables.Disposable
import java.io.File

interface DataRepository {

    /**
     * 设置当前检测任务的文件夹
     * @param dir 文件夹
     */
    fun setCheckFileDir(dir: File)

    /**
     * 获取当前检测任务的文件夹
     * @return 文件夹
     */
    fun getCheckFileDir(): File?

    /**
     * 获取图表数据
     */
    fun getPhaseData(chartType: Int): ArrayList<HashMap<Int, Float>>

    /**
     * 实时数据监测
     */
    fun realDataListener()

    /**
     * 移除实时数据回调
     */
    fun removeRealDataListener()

    /**
     * 设置实时数据的回调
     * @param callback 回调
     */
    fun addRealDataCallback(callback: RealDataCallback)

    /**
     * 切换实时数据监测通道
     */
    fun switchPassageway(passageway: Int)

    fun addHufData(callback: DataCallback)

    interface DataCallback {

        fun addData(map: HashMap<Int, Float>, prPsCube: PrPsCubeList)
    }

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
    fun getGainValueList(): MutableLiveData<List<Float>>

    /**
     * 循环读取遥测信息
     */
    fun startCycleReadYcValue(): Disposable


}