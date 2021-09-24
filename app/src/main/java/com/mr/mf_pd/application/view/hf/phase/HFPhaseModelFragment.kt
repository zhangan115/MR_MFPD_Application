package com.mr.mf_pd.application.view.hf.phase

import android.os.Bundle
import androidx.fragment.app.viewModels
import com.mr.mf_pd.application.R
import com.mr.mf_pd.application.common.ConstantStr
import com.mr.mf_pd.application.databinding.HFPhaseDataBinding
import com.mr.mf_pd.application.model.DeviceBean
import com.mr.mf_pd.application.view.ac.phase.ACPhaseModelFragment
import com.mr.mf_pd.application.view.base.BaseFragment
import com.mr.mf_pd.application.view.base.ext.getViewModelFactory
import com.mr.mf_pd.application.view.tev.phase.TEVPhaseModelViewModel

/**
 * HF 相位模式
 */
class HFPhaseModelFragment : BaseFragment<HFPhaseDataBinding>() {

    private val viewModel by viewModels<TEVPhaseModelViewModel> { getViewModelFactory() }
    private var rendererSet = false

    companion object {

        fun create(deviceBean: DeviceBean?): ACPhaseModelFragment {
            val fragment = ACPhaseModelFragment()
            val bundle = Bundle()
            bundle.putParcelable(ConstantStr.KEY_BUNDLE_OBJECT, deviceBean)
            fragment.arguments = bundle
            return fragment
        }
    }


    override fun lazyLoad() {
        viewModel.start()
    }

    override fun getContentView(): Int {
        return R.layout.fragment_hf_phase
    }

    override fun initData() {

    }


    override fun initView() {

    }

    override fun setViewModel(dataBinding: HFPhaseDataBinding?) {
        dataBinding?.vm = viewModel
    }

    override fun onResume() {
        super.onResume()
        if (rendererSet) {

        }
    }

    override fun onPause() {
        super.onPause()
        if (rendererSet) {

        }
    }
}