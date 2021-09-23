package com.mr.mf_pd.application.view.ac.flight

import android.os.Bundle
import androidx.fragment.app.viewModels
import com.mr.mf_pd.application.R
import com.mr.mf_pd.application.common.ConstantStr
import com.mr.mf_pd.application.databinding.ACFlightDataBinding
import com.mr.mf_pd.application.model.DeviceBean

import com.mr.mf_pd.application.view.base.BaseFragment
import com.mr.mf_pd.application.view.base.ext.getViewModelFactory

/**
 * AC 飞行模式
 */
class ACFlightFragment : BaseFragment<ACFlightDataBinding>() {

    private val viewModel by viewModels<ACFlightModelViewModel> { getViewModelFactory() }
    private var rendererSet = false

    companion object {

        fun create(deviceBean: DeviceBean?): ACFlightFragment {
            val fragment = ACFlightFragment()
            val bundle = Bundle()
            bundle.putParcelable(ConstantStr.KEY_BUNDLE_OBJECT, deviceBean)
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun lazyLoad() {

    }

    override fun getContentView(): Int {
        return R.layout.fragment_ac_flight
    }

    override fun initData() {

    }

    override fun initView() {

    }

    override fun setViewModel(dataBinding: ACFlightDataBinding?) {
        dataBinding?.vm = viewModel
    }
}