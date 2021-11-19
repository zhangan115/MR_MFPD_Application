package com.mr.mf_pd.application.view.check.ac.setting

import android.os.Bundle
import android.widget.LinearLayout
import androidx.activity.viewModels
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.afollestad.materialdialogs.list.listItems
import com.mr.mf_pd.application.R
import com.mr.mf_pd.application.databinding.ACSettingDataBinding
import com.mr.mf_pd.application.utils.getViewModelFactory
import com.mr.mf_pd.application.view.base.BaseSettingActivity
import kotlinx.android.synthetic.main.activity_ac_setting.*
import kotlinx.android.synthetic.main.activity_uhf_setting.*
import kotlinx.android.synthetic.main.activity_uhf_setting.phaseModelLayout

class ACSettingActivity : BaseSettingActivity<ACSettingDataBinding>() {

    private val viewModel by viewModels<ACSettingViewModel> { getViewModelFactory() }

    override fun initView(savedInstanceState: Bundle?) {
        fzUnitLayout.setOnClickListener {
            //检测频带
            MaterialDialog(this)
                .show {
                    listItems(R.array.fz_unit_list) { _, index, text ->
                        viewModel.fzUnitStr.postValue(text.toString())
                        viewModel.checkType.settingBean.fzUnit = index
                    }
                    lifecycleOwner(this@ACSettingActivity)
                }
        }
    }

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