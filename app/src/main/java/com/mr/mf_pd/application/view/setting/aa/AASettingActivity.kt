package com.mr.mf_pd.application.view.setting.aa

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import androidx.activity.viewModels
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.mr.mf_pd.application.R
import com.mr.mf_pd.application.common.ConstantStr
import com.mr.mf_pd.application.common.Constants
import com.mr.mf_pd.application.databinding.AASettingDataBinding
import com.mr.mf_pd.application.utils.getViewModelFactory
import com.mr.mf_pd.application.view.base.BaseSettingActivity
import kotlinx.android.synthetic.main.activity_uhf_setting.*

class AASettingActivity : BaseSettingActivity<AASettingDataBinding>() {

    private val viewModel by viewModels<AASettingViewModel> { getViewModelFactory() }

    override fun initData(savedInstanceState: Bundle?) {
        super.initData(savedInstanceState)
        dataBinding.vm = viewModel
        viewModel.start(checkType)
    }

    override fun getContentView(): Int {
        return R.layout.activity_aa_setting
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
        val intent = Intent(Constants.UPDATE_SETTING)
        intent.putExtra(ConstantStr.KEY_BUNDLE_OBJECT,viewModel.checkType.settingBean)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }
}