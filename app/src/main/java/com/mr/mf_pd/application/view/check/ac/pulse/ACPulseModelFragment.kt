package com.mr.mf_pd.application.view.check.ac.pulse

import android.os.Bundle
import androidx.fragment.app.viewModels
import com.mr.mf_pd.application.R
import com.mr.mf_pd.application.common.ConstantStr
import com.mr.mf_pd.application.databinding.ACPulseDataBinding
import com.mr.mf_pd.application.model.DeviceBean

import com.mr.mf_pd.application.view.base.BaseFragment
import com.mr.mf_pd.application.view.base.ext.getViewModelFactory

/**
 * AC 脉冲波形
 */
class ACPulseModelFragment : BaseFragment<ACPulseDataBinding>() {

    private val viewModel by viewModels<ACPulseModelViewModel> { getViewModelFactory() }
    private var rendererSet = false

    companion object {

        fun create(deviceBean: DeviceBean?): ACPulseModelFragment {
            val fragment = ACPulseModelFragment()
            val bundle = Bundle()
            bundle.putParcelable(ConstantStr.KEY_BUNDLE_OBJECT, deviceBean)
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun lazyLoad() {

    }

    override fun getContentView(): Int {
        return R.layout.fragment_ac_pulse
    }

    override fun initData() {

    }

    override fun initView() {

    }

    override fun setViewModel(dataBinding: ACPulseDataBinding?) {
        dataBinding?.vm = viewModel
    }
}