package com.mr.mf_pd.application.view.check.pulse

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.viewModels
import com.mr.mf_pd.application.R
import com.mr.mf_pd.application.databinding.ACPulseDataBinding
import com.mr.mf_pd.application.repository.DefaultDataRepository
import com.mr.mf_pd.application.repository.DefaultFilesRepository
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

        fun create(): ACPulseModelFragment {
            val fragment = ACPulseModelFragment()
            val bundle = Bundle()
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
        if (viewModel.checkType.settingBean.gdCd == 1){
            viewModel.gainMinValue.postValue(viewModel.checkType.settingBean.minValue.toFloat())
        }else{
            if (viewModel.isFile.value!!) {
                viewModel.gainMinValue.postValue(DefaultFilesRepository.realDataMinValue.value?.toFloat())
            } else {
                viewModel.gainMinValue.postValue(DefaultDataRepository.realDataMinValue.value?.toFloat())
            }
        }
    }

    override fun setIsFileValue(isFile: Boolean?) {
        viewModel.isFile.value = isFile
    }

    override fun initView() {
        activity?.let {
            LineChartUtils.initChart(lineChart, it.applicationContext)
        }
    }

    override fun setViewModel(dataBinding: ACPulseDataBinding?) {
        dataBinding?.vm = viewModel
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

    override fun onYcDataChange(bytes: ByteArray) {

    }

    override fun cleanCurrentData() {

    }

    override fun isAdd(): Boolean {
        return isAdded
    }

}