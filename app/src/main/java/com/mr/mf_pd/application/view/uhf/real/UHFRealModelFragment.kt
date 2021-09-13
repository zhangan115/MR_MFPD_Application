package com.mr.mf_pd.application.view.uhf.real

import android.os.Bundle
import androidx.fragment.app.viewModels
import com.mr.mf_pd.application.R
import com.mr.mf_pd.application.common.ConstantStr
import com.mr.mf_pd.application.databinding.UHFRealDataBinding
import com.mr.mf_pd.application.model.DeviceBean
import com.mr.mf_pd.application.view.base.BaseFragment
import com.mr.mf_pd.application.view.base.ext.getViewModelFactory

class UHFRealModelFragment : BaseFragment<UHFRealDataBinding>() {

    private val viewModel by viewModels<UHFRealModelViewModel> { getViewModelFactory() }

    companion object {

        fun create(deviceBean: DeviceBean?): UHFRealModelFragment {
            val fragment = UHFRealModelFragment()
            val bundle = Bundle()
            bundle.putParcelable(ConstantStr.KEY_BUNDLE_OBJECT, deviceBean)
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun lazyLoad() {

    }

    override fun getContentView(): Int {
        return R.layout.fragment_uhf_real
    }

    override fun initData() {

    }

    override fun initView() {

    }

    override fun setViewModel(dataBinding: UHFRealDataBinding?) {
        dataBinding?.vm = viewModel
    }
}