package com.mr.mf_pd.application.view.uhf.setting

import android.os.Bundle
import androidx.activity.viewModels
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.afollestad.materialdialogs.list.listItems
import com.mr.mf_pd.application.R
import com.mr.mf_pd.application.databinding.UHFSettingDataBinding
import com.mr.mf_pd.application.utils.getViewModelFactory
import com.mr.mf_pd.application.view.base.AbsBaseActivity
import kotlinx.android.synthetic.main.activity_uhf_setting.*

class UHFSettingActivity : AbsBaseActivity<UHFSettingDataBinding>() {

    private val viewModel by viewModels<UHFSettingViewModel> { getViewModelFactory() }

    override fun initView(savedInstanceState: Bundle?) {
        phaseModelLayout.setOnClickListener {
            //相位同步
            MaterialDialog(this)
                .show {
                    listItems(R.array.choose_phase_model) { _, index, text ->
                        text.let {
                            viewModel.phaseModelStr.postValue(it.toString())
                        }
                        viewModel.phaseModelInt.postValue(index)
                    }
                    lifecycleOwner(this@UHFSettingActivity)
                }
        }
        bandDetectionLayout.setOnClickListener {
            //检测频带
            MaterialDialog(this)
                .show {
                    listItems(R.array.choose_band_detection) { _, index, text ->
                        text.let {
                            viewModel.bandDetectionStr.postValue(it.toString())
                        }
                        viewModel.bandDetectionInt.postValue(index)
                    }
                    lifecycleOwner(this@UHFSettingActivity)
                }
        }
    }

    override fun initData(savedInstanceState: Bundle?) {
        dataBinding.vm = viewModel
        viewModel.start()
    }

    override fun getToolBarTitle(): String {
        return "特高频设置"
    }

    override fun getContentView(): Int {
        return R.layout.activity_uhf_setting
    }
}