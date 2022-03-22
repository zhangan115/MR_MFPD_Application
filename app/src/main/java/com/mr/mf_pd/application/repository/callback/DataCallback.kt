package com.mr.mf_pd.application.repository.callback

import com.mr.mf_pd.application.view.opengl.`object`.PrPsCubeList

interface DataCallback {

    fun addData(map: HashMap<Int, Float?>, prPsCube: PrPsCubeList)
}