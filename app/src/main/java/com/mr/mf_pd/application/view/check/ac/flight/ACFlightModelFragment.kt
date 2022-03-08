package com.mr.mf_pd.application.view.check.ac.flight

import android.os.Bundle
import androidx.fragment.app.viewModels
import com.mr.mf_pd.application.R
import com.mr.mf_pd.application.common.ConstantStr
import com.mr.mf_pd.application.databinding.ACFlightDataBinding
import com.mr.mf_pd.application.model.DeviceBean
import com.mr.mf_pd.application.utils.LineChartUtils
import com.mr.mf_pd.application.view.base.BaseCheckFragment

import com.mr.mf_pd.application.view.base.ext.getViewModelFactory
import kotlinx.android.synthetic.main.fragment_ac_flight.*

/**
 * AC 飞行模式
 */
class ACFlightModelFragment : BaseCheckFragment<ACFlightDataBinding>() {

    private val viewModel by viewModels<ACFlightModelViewModel> { getViewModelFactory() }

    companion object {

        fun create(): ACFlightModelFragment {
            val fragment = ACFlightModelFragment()
            val bundle = Bundle()
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun lazyLoad() {
        viewModel.start()
    }

    override fun getContentView(): Int {
        return R.layout.fragment_ac_flight
    }

    override fun initData() {

    }

    override fun initView() {
        activity?.let {
            LineChartUtils.initChart(lineChart, it.applicationContext)
        }
    }

    override fun onYcDataChange(bytes: ByteArray) {

    }

    override fun setViewModel(dataBinding: ACFlightDataBinding?) {
        dataBinding?.vm = viewModel
    }


    override fun isSaving(): Boolean {
        return false
    }

    override fun cancelSaveData() {

    }
}