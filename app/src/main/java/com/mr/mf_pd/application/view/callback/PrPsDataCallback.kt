package com.mr.mf_pd.application.view.callback

import java.util.concurrent.CopyOnWriteArrayList

interface PrPsDataCallback {
    fun prpsDataChange(data: HashMap<Int, HashMap<Float, Int>>, cubeList: CopyOnWriteArrayList<Float?>)
}