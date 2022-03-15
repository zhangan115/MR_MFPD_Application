package com.mr.mf_pd.application.view.check.ac.flight

import android.os.Bundle
import androidx.fragment.app.viewModels
import com.mr.mf_pd.application.R
import com.mr.mf_pd.application.common.ConstantStr
import com.mr.mf_pd.application.databinding.ACFlightDataBinding
import com.mr.mf_pd.application.model.DeviceBean
import com.mr.mf_pd.application.repository.DefaultDataRepository
import com.mr.mf_pd.application.repository.DefaultFilesRepository
import com.mr.mf_pd.application.utils.LineChartUtils
import com.mr.mf_pd.application.view.base.BaseCheckFragment

import com.mr.mf_pd.application.view.base.ext.getViewModelFactory
import kotlinx.android.synthetic.main.fragment_ac_flight.*

/**
 * AC 飞行模式
 */
class ACFlightModelFragment : BaseCheckFragment<ACFlightDataBinding>() {

    private val viewModel by viewModels<ACFlightModelViewModel> { getViewModelFactory() }

    companion object {

        fun create(): ACFlightModelFragment {
            val fragment = ACFlightModelFragment()
            val bundle = Bundle()
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun lazyLoad() {
        viewModel.start()
    }

    override fun getContentView(): Int {
        return R.layout.fragment_ac_flight
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

    override fun onYcDataChange(bytes: ByteArray) {

    }

    override fun cleanCurrentData() {

    }

    override fun setViewModel(dataBinding: ACFlightDataBinding?) {
        dataBinding?.vm = viewModel
    }


    override fun isSaving(): Boolean {
        return false
    }

    override fun cancelSaveData() {

    }

    override fun isAdd(): Boolean {
        return isAdded
    }

}