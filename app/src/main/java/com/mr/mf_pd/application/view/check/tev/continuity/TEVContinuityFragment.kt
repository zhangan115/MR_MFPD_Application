package com.mr.mf_pd.application.view.check.tev.continuity

import android.os.Bundle
import androidx.fragment.app.viewModels
import com.mr.mf_pd.application.R
import com.mr.mf_pd.application.common.ConstantStr
import com.mr.mf_pd.application.databinding.ACContinuityDataBinding
import com.mr.mf_pd.application.databinding.TEVContinuityDataBinding
import com.mr.mf_pd.application.model.DeviceBean
import com.mr.mf_pd.application.view.check.ac.continuity.ACContinuityFragment

import com.mr.mf_pd.application.view.base.BaseFragment
import com.mr.mf_pd.application.view.base.ext.getViewModelFactory

/**
 * AC 连续模式
 */
class TEVContinuityFragment : BaseFragment<TEVContinuityDataBinding>() {

    private val viewModel by viewModels<TEVContinuityModelViewModel> { getViewModelFactory() }
    private var rendererSet = false

    companion object {

        fun create(deviceBean: DeviceBean?): ACContinuityFragment {
            val fragment = ACContinuityFragment()
            val bundle = Bundle()
            bundle.putParcelable(ConstantStr.KEY_BUNDLE_OBJECT, deviceBean)
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun lazyLoad() {

    }

    override fun getContentView(): Int {
        return R.layout.fragment_tev_continuity
    }

    override fun initData() {

    }

    override fun initView() {

    }

    override fun setViewModel(dataBinding: TEVContinuityDataBinding?) {
        dataBinding?.vm = viewModel
    }
}