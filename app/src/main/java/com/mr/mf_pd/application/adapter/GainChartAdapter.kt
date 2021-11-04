package com.mr.mf_pd.application.adapter

import androidx.databinding.BindingAdapter
import com.mr.mf_pd.application.widget.GainChartView

object GainChartAdapter {

    @JvmStatic
    @BindingAdapter(
        "app:gain_value"
    )
    fun bindGainValue(
        view: GainChartView,
        values: List<Float>
    ) {
        view.updateData(values)
    }
}