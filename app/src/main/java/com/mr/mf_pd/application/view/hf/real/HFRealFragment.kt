package com.mr.mf_pd.application.view.hf.real

import android.os.Bundle
import androidx.fragment.app.viewModels
import com.mr.mf_pd.application.R
import com.mr.mf_pd.application.common.ConstantStr
import com.mr.mf_pd.application.databinding.HFRealDataBinding
import com.mr.mf_pd.application.model.DeviceBean
import com.mr.mf_pd.application.view.ac.real.ACRealFragment

import com.mr.mf_pd.application.view.base.BaseFragment
import com.mr.mf_pd.application.view.base.ext.getViewModelFactory
import com.mr.mf_pd.application.view.tev.real.TEVRealModelViewModel

/**
 * HF 实时模式
 */
class HFRealFragment : BaseFragment<HFRealDataBinding>() {

    private val viewModel by viewModels<TEVRealModelViewModel> { getViewModelFactory() }
    private var rendererSet = false

    companion object {

        fun create(deviceBean: DeviceBean?): ACRealFragment {
            val fragment = ACRealFragment()
            val bundle = Bundle()
            bundle.putParcelable(ConstantStr.KEY_BUNDLE_OBJECT, deviceBean)
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun lazyLoad() {

    }

    override fun getContentView(): Int {
        return R.layout.fragment_hf_real
    }

    override fun initData() {

    }

    override fun initView() {

    }

    override fun setViewModel(dataBinding: HFRealDataBinding?) {
        dataBinding?.vm = viewModel
    }
}