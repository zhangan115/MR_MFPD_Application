package com.mr.mf_pd.application.adapter

import androidx.databinding.BindingAdapter
import com.mr.mf_pd.application.common.CheckType
import com.mr.mf_pd.application.model.CheckParamsBean
import com.mr.mf_pd.application.widget.CheckParamsView

object CheckParamsAdapter {

    @JvmStatic
    @BindingAdapter(
        "app:check_params"
    )
    fun bindCheckParams(
        view: CheckParamsView,
        type: CheckParamsBean
    ) {
        view.setData(type)
    }
}