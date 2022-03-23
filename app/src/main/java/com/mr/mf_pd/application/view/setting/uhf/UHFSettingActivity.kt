package com.mr.mf_pd.application.view.setting.uhf

import android.os.Bundle
import android.widget.LinearLayout
import androidx.activity.viewModels
import com.google.common.eventbus.EventBus
import com.mr.mf_pd.application.R
import com.mr.mf_pd.application.databinding.UHFSettingDataBinding

import com.mr.mf_pd.application.utils.getViewModelFactory
import com.mr.mf_pd.application.view.base.BaseSettingActivity
import kotlinx.android.synthetic.main.activity_uhf_setting.*

class UHFSettingActivity : BaseSettingActivity<UHFSettingDataBinding>() {

    private val viewModel by viewModels<UHFSettingViewModel> { getViewModelFactory() }

    override fun initData(savedInstanceState: Bundle?) {
        super.initData(savedInstanceState)
        dataBinding.vm = viewModel
        viewModel.start(checkType)
    }

    override fun getContentView(): Int {
        return R.layout.activity_uhf_setting
    }

    override fun getPhaseModelLayout(): LinearLayout {
        return phaseModelLayout
    }

    override fun getBandDetectionLayout(): LinearLayout {
        return bandDetectionLayout
    }

    override fun onPhaseModelChange(text:String,index:Int) {
        viewModel.checkType.settingBean.xwTb = index
        viewModel.phaseModelStr.postValue(text)
        viewModel.phaseModelInt.postValue(index)
    }

    override fun onBandDetectionChange(text:String,index:Int) {
        viewModel.checkType.settingBean.pdJc = index
        viewModel.bandDetectionStr.postValue(text)
        viewModel.bandDetectionInt.postValue(index)
    }

    override fun onPause() {
        super.onPause()
        viewModel.toSave()
        EventBus().post(viewModel.checkType.settingBean)
    }
}