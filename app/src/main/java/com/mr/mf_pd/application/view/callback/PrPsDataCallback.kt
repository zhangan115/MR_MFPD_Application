package com.mr.mf_pd.application.view.callback

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

interface PrPsDataCallback {
    fun prpsDataChange(data: ConcurrentHashMap<Int, ConcurrentHashMap<Float, Int>>, cubeList: CopyOnWriteArrayList<Float?>)
}