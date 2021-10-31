package com.mr.mf_pd.application.repository.impl

import com.mr.mf_pd.application.model.ACModelBean
import com.mr.mf_pd.application.model.UHFModelBean
import com.mr.mf_pd.application.view.opengl.`object`.PrPsCubeList
import java.io.File

interface DataRepository {

    /**
     * 开始保存数据
     */
    fun startSaveData()

    /**
     * 停止保存
     */
    fun stopSaveData()

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
     * 获取特高频HUF数据
     * @return HUF类
     */
    fun getHufData(): UHFModelBean?

    /**
     * 获取图表数据
     */
    fun getPhaseData(chartType:Int): ArrayList<HashMap<Int, Float>>

    /**
     * 获取缓冲的图表数据
     */
    fun getCachePhaseData(chartType:Int): ArrayList<HashMap<Int, Float>>

    /**
     * 获取超波AC数据
     * @return AC类
     */
    fun getAcData(): ACModelBean?

    fun hufDataListener()

    fun addHufData(callback: DataCallback)

    fun removeHufDataListener()

    fun switchPassageway(passageway: Int)

    interface DataCallback {

        fun addData(map: HashMap<Int, Float>, prPsCube: PrPsCubeList)
    }

    fun setRealDataCallback(callback: RealDataCallback)


    interface RealDataCallback {

        fun update(prPsCube: PrPsCubeList)
    }

    fun cleanData()
}