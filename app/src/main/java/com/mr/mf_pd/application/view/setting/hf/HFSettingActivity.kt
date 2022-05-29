package com.mr.mf_pd.application.view.setting.hf

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.SeekBar
import androidx.activity.viewModels
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.mr.mf_pd.application.R
import com.mr.mf_pd.application.common.ConstantInt
import com.mr.mf_pd.application.common.ConstantStr
import com.mr.mf_pd.application.common.Constants
import com.mr.mf_pd.application.databinding.HFSettingDataBinding
import com.mr.mf_pd.application.utils.getViewModelFactory
import com.mr.mf_pd.application.view.base.BaseSettingActivity
import kotlinx.android.synthetic.main.activity_hf_setting.*

class HFSettingActivity : BaseSettingActivity<HFSettingDataBinding>() {

    private val viewModel by viewModels<HFSettingViewModel> { getViewModelFactory() }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        calibrationButton.setOnClickListener {

        }
        viewModel.limitProgressValue.observe(this){
            limitValueProgressBar.setProgress(it,false)
        }
        limitValueProgressBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    val value = ConstantInt.LIMIT_VALUE_MAX * progress / 100
                    limitValueEt.setText(value.toString())
                    viewModel.limitValueStr.postValue(value.toString())
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
//                limitValueEt.removeTextChangedListener(textWatcher)
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

//                limitValueEt.addTextChangedListener(textWatcher)
            }
        })

    }

    override fun initData(savedInstanceState: Bundle?) {
        super.initData(savedInstanceState)
        dataBinding.vm = viewModel
        viewModel.start(checkType)
    }


    override fun getContentView(): Int {
        return R.layout.activity_hf_setting
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