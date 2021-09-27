package com.mr.mf_pd.application.view.check.ac.real

import android.os.Bundle
import androidx.fragment.app.viewModels
import com.mr.mf_pd.application.R
import com.mr.mf_pd.application.common.ConstantStr
import com.mr.mf_pd.application.databinding.ACRealDataBinding
import com.mr.mf_pd.application.model.DeviceBean

import com.mr.mf_pd.application.view.base.BaseFragment
import com.mr.mf_pd.application.view.base.ext.getViewModelFactory
import com.mr.mf_pd.application.view.check.tev.real.TEVRealFragment
import com.mr.mf_pd.application.view.check.tev.real.TEVRealModelViewModel

/**
 * AC 实时模式
 */
class ACRealFragment : BaseFragment<ACRealDataBinding>() {

    private val viewModel by viewModels<TEVRealModelViewModel> { getViewModelFactory() }
    private var rendererSet = false

    companion object {

        fun create(deviceBean: DeviceBean?): TEVRealFragment {
            val fragment = TEVRealFragment()
            val bundle = Bundle()
            bundle.putParcelable(ConstantStr.KEY_BUNDLE_OBJECT, deviceBean)
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun lazyLoad() {

    }

    override fun getContentView(): Int {
        return R.layout.fragment_ac_real
    }

    override fun initData() {

    }

    override fun initView() {

    }

    override fun setViewModel(dataBinding: ACRealDataBinding?) {
        dataBinding?.vm = viewModel
    }
}