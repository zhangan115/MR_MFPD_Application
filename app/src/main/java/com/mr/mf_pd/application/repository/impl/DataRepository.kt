package com.mr.mf_pd.application.repository.impl

import com.mr.mf_pd.application.model.ACModelBean
import com.mr.mf_pd.application.model.UHFModelBean
import com.mr.mf_pd.application.view.opengl.`object`.PrPsCubeList

interface DataRepository {

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