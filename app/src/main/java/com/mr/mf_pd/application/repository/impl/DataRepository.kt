package com.mr.mf_pd.application.repository.impl

import com.mr.mf_pd.application.model.ACModelBean
import com.mr.mf_pd.application.model.UHFModelBean
import com.mr.mf_pd.application.view.opengl.`object`.PrPsCubeList
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
     * 获取特高频HUF数据
     * @return HUF类
     */
    fun getHufData(): UHFModelBean?

    /**
     * 获取超波AC数据
     * @return AC类
     */
    fun getAcData(): ACModelBean?

    fun hufDataListener()

    fun addHufData(callback: DataCallback)

    fun removeHufDataListener()

    fun toSaveData()

    fun switchPassageway(passageway: Int)

    interface DataCallback {

        fun addData(map: HashMap<Int, Float>, prPsCube: PrPsCubeList)
    }
}