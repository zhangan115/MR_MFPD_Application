package com.mr.mf_pd.application.view.fragment.continuity

import android.os.Bundle
import androidx.fragment.app.viewModels
import com.mr.mf_pd.application.R
import com.mr.mf_pd.application.common.ConstantStr
import com.mr.mf_pd.application.databinding.ContinuityDataBinding
import com.mr.mf_pd.application.model.DeviceBean
import com.mr.mf_pd.application.utils.LineChartUtils
import com.mr.mf_pd.application.view.base.BaseCheckFragment

import com.mr.mf_pd.application.view.base.BaseFragment
import com.mr.mf_pd.application.view.base.ext.getViewModelFactory
import kotlinx.android.synthetic.main.fragment_continuity.*

/**
 * 连续模式
 */
class ContinuityModelFragment : BaseCheckFragment<ContinuityDataBinding>() {

    private val viewModel by viewModels<ContinuityModelViewModel> { getViewModelFactory() }

    companion object {

        fun create(deviceBean: DeviceBean?): ContinuityModelFragment {
            val fragment = ContinuityModelFragment()
            val bundle = Bundle()
            bundle.putParcelable(ConstantStr.KEY_BUNDLE_OBJECT, deviceBean)
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun lazyLoad() {

    }

    override fun getContentView(): Int {
        return R.layout.fragment_continuity
    }

    override fun initData() {

    }

    override fun initView() {
        LineChartUtils.initNoAxisChart(lineChart1)
        LineChartUtils.initNoAxisChart(lineChart2)
        LineChartUtils.initNoAxisChart(lineChart3)
        LineChartUtils.initNoAxisChart(lineChart4)
        image1.setOnClickListener {

        }
        image2.setOnClickListener {

        }
        image3.setOnClickListener {

        }
        image4.setOnClickListener {

        }
    }

    override fun setViewModel(dataBinding: ContinuityDataBinding?) {
        dataBinding?.vm = viewModel
    }

    override fun isSaving(): Boolean {
        return false
    }

    override fun cancelSaveData() {

    }


}