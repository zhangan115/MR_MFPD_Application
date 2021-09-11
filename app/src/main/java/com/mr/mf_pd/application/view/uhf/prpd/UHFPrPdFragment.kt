package com.mr.mf_pd.application.view.uhf.prpd

import android.os.Bundle
import androidx.fragment.app.viewModels
import com.mr.mf_pd.application.R
import com.mr.mf_pd.application.common.ConstantStr
import com.mr.mf_pd.application.databinding.UHFPrPdDataBinding
import com.mr.mf_pd.application.model.DeviceBean
import com.mr.mf_pd.application.view.base.BaseFragment
import com.mr.mf_pd.application.view.base.ext.getViewModelFactory

class UHFPrPdFragment : BaseFragment<UHFPrPdDataBinding>() {

    private val viewModel by viewModels<UHFPrPdViewModel> { getViewModelFactory() }

    companion object {

        fun create(deviceBean: DeviceBean?): UHFPrPdFragment {
            val fragment = UHFPrPdFragment()
            val bundle = Bundle()
            bundle.putParcelable(ConstantStr.KEY_BUNDLE_OBJECT, deviceBean)
            fragment.arguments = bundle
            return fragment
        }
    }


    override fun lazyLoad() {

    }

    override fun getContentView(): Int {
        return R.layout.fragment_uhf_prpd
    }

    override fun initData() {

    }

    override fun initView() {

    }

    override fun setViewModel(dataBinding: UHFPrPdDataBinding?) {
        dataBinding?.vm = viewModel
    }
}