package com.mr.mf_pd.application.view.check.ac.pulse

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.viewModels
import com.mr.mf_pd.application.R
import com.mr.mf_pd.application.common.ConstantStr
import com.mr.mf_pd.application.databinding.ACPulseDataBinding
import com.mr.mf_pd.application.model.DeviceBean
import com.mr.mf_pd.application.utils.LineChartUtils
import com.mr.mf_pd.application.view.base.BaseCheckFragment

import com.mr.mf_pd.application.view.base.ext.getViewModelFactory
import kotlinx.android.synthetic.main.fragment_ac_pulse.*

/**
 * AC 脉冲波形
 */
class ACPulseModelFragment : BaseCheckFragment<ACPulseDataBinding>() {

    private val viewModel by viewModels<ACPulseModelViewModel> { getViewModelFactory() }

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
        viewModel.start()
    }

    override fun getContentView(): Int {
        return R.layout.fragment_ac_pulse
    }

    override fun initData() {

    }

    override fun initView() {
        activity?.let {
            LineChartUtils.initChart(lineChart, it.applicationContext)
        }
    }

    override fun setViewModel(dataBinding: ACPulseDataBinding?) {
        dataBinding?.vm = viewModel
    }

    override fun toSaveData2File() {

    }

    override fun isSaving(): Boolean {
        return false
    }

    override fun cancelSaveData() {

    }

    override fun onResume() {
        super.onResume()
        Log.d("zhangan", "onResume")
        viewModel.openPulseData()
    }

    override fun onPause() {
        super.onPause()
        Log.d("zhangan", "onPause")
        viewModel.closePulseData()
    }
}