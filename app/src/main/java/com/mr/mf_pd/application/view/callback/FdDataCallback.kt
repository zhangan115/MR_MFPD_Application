package com.mr.mf_pd.application.view.callback

import java.util.concurrent.CopyOnWriteArrayList

interface FdDataCallback {
    fun fdData(data: CopyOnWriteArrayList<Float?>)
}