package com.mr.mf_pd.application.repository.impl

import com.mr.mf_pd.application.model.UHFModelBean
import com.mr.mf_pd.application.view.opengl.`object`.PrPsCubeList
import com.mr.mf_pd.application.view.opengl.`object`.PrPsXZPoints

interface DataRepository {

    fun getHufData(): UHFModelBean?

    fun hufDataListener()

    fun addHufData(callback: DataCallback)

    fun removeHufDataListener()

    fun toSaveData()

    interface DataCallback {
        fun addData(data: PrPsXZPoints)
        fun addData(prPsCube: PrPsCubeList)
    }
}