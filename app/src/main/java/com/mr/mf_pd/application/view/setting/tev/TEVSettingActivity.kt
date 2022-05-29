package com.mr.mf_pd.application.view.setting.tev

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.LinearLayout
import android.widget.SeekBar
import androidx.activity.viewModels
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.mr.mf_pd.application.R
import com.mr.mf_pd.application.common.ConstantInt
import com.mr.mf_pd.application.common.ConstantStr
import com.mr.mf_pd.application.common.Constants
import com.mr.mf_pd.application.databinding.TEVSettingDataBinding
import com.mr.mf_pd.application.utils.getViewModelFactory
import com.mr.mf_pd.application.view.base.BaseSettingActivity
import kotlinx.android.synthetic.main.activity_tev_setting.*

class TEVSettingActivity : BaseSettingActivity<TEVSettingDataBinding>() {

    private val viewModel by viewModels<TEVSettingViewModel> { getViewModelFactory() }

    override fun initData(savedInstanceState: Bundle?) {
        super.initData(savedInstanceState)
        dataBinding.vm = viewModel
        viewModel.start(checkType)
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
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

    private val textWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

        }

        override fun afterTextChanged(s: Editable?) {
            viewModel.limitValueStr.value?.let {
                val value = limitValueEt.text.toString().toIntOrNull()
                if (value != null) {
                    val progress = (value / ConstantInt.LIMIT_VALUE_MAX).toFloat() * 100.00f
                    Log.d("zhangan","value is $value progress value is  ${progress.toInt()}")
                    limitValueProgressBar.setProgress(progress.toInt(), true)
                }
            }
        }

    }

    override fun getContentView(): Int {
        return R.layout.activity_tev_setting
    }

    override fun getPhaseModelLayout(): LinearLayout {
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
        intent.putExtra(ConstantStr.KEY_BUNDLE_OBJECT, viewModel.checkType.settingBean)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }
}