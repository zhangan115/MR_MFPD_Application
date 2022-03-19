package com.mr.mf_pd.application.view.setting.ae

import android.os.Bundle
import android.widget.LinearLayout
import androidx.activity.viewModels
import com.mr.mf_pd.application.R
import com.mr.mf_pd.application.databinding.ACSettingDataBinding
import com.mr.mf_pd.application.utils.getViewModelFactory
import com.mr.mf_pd.application.view.base.BaseSettingActivity
import kotlinx.android.synthetic.main.activity_uhf_setting.phaseModelLayout

class AESettingActivity : BaseSettingActivity<ACSettingDataBinding>() {

    private val viewModel by viewModels<AESettingViewModel> { getViewModelFactory() }

    override fun initData(savedInstanceState: Bundle?) {
        super.initData(savedInstanceState)
        dataBinding.vm = viewModel
        viewModel.start(checkType)
    }

    override fun getContentView(): Int {
        return R.layout.activity_ac_setting
    }

    override fun getPhaseModelLayout(): LinearLayout? {
        return phaseModelLayout
    }

    override fun getBandDetectionLayout(): LinearLayout? {
        return null
    }

    override fun onPhaseModelChange(text: String, index: Int) {
        viewModel.checkType.settingBean.xwTb = index
        viewModel.phaseModelStr.postValue(text)
        viewModel.phaseModelInt.postValue(index)
    }

    override fun onBandDetectionChange(text: String, index: Int) {

    }


    override fun onPause() {
        super.onPause()
        viewModel.toSave()
    }
}