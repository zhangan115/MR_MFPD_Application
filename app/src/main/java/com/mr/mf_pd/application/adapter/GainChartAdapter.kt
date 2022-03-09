package com.mr.mf_pd.application.adapter

import androidx.databinding.BindingAdapter
import com.mr.mf_pd.application.widget.GainChartView

object GainChartAdapter {

    @JvmStatic
    @BindingAdapter(
        "app:gain_value","app:gain_minValue"
    )
    fun bindGainValue(
        view: GainChartView,
        values: ArrayList<Float>,
        minValue:Float?
    ) {
        view.updateData(values,minValue)
    }
}