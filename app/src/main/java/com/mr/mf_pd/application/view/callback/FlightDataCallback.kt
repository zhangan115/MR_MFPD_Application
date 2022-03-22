package com.mr.mf_pd.application.view.callback

interface FlightDataCallback {
    fun flightData(data: HashMap<Int, HashMap<Float, Int>>)
}