package com.mr.mf_pd.application.repository.impl

import com.mr.mf_pd.application.model.ACModelBean
import com.mr.mf_pd.application.model.UHFModelBean
import com.mr.mf_pd.application.view.opengl.`object`.PrPsCubeList

interface DataRepository {

    fun getHufData(): UHFModelBean?

    fun getAcData(): ACModelBean?

    fun hufDataListener()

    fun addHufData(callback: DataCallback)

    fun removeHufDataListener()

    fun toSaveData()

    interface DataCallback {

        fun addData(map: HashMap<Int, Float>, prPsCube: PrPsCubeList)
    }
}