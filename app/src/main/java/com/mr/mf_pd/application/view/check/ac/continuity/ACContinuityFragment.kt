package com.mr.mf_pd.application.view.check.ac.continuity

import android.os.Bundle
import androidx.fragment.app.viewModels
import com.mr.mf_pd.application.R
import com.mr.mf_pd.application.common.ConstantStr
import com.mr.mf_pd.application.databinding.ACContinuityDataBinding
import com.mr.mf_pd.application.model.DeviceBean

import com.mr.mf_pd.application.view.base.BaseFragment
import com.mr.mf_pd.application.view.base.ext.getViewModelFactory
import com.mr.mf_pd.application.view.check.tev.continuity.TEVContinuityFragment
import com.mr.mf_pd.application.view.check.tev.continuity.TEVContinuityModelViewModel

/**
 * AC 连续模式
 */
class ACContinuityFragment : BaseFragment<ACContinuityDataBinding>() {

    private val viewModel by viewModels<TEVContinuityModelViewModel> { getViewModelFactory() }
    private var rendererSet = false

    companion object {

        fun create(deviceBean: DeviceBean?): TEVContinuityFragment {
            val fragment = TEVContinuityFragment()
            val bundle = Bundle()
            bundle.putParcelable(ConstantStr.KEY_BUNDLE_OBJECT, deviceBean)
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun lazyLoad() {

    }

    override fun getContentView(): Int {
        return R.layout.fragment_ac_continuity
    }

    override fun initData() {

    }

    override fun initView() {

    }

    override fun setViewModel(dataBinding: ACContinuityDataBinding?) {
        dataBinding?.vm = viewModel
    }
}