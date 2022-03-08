package com.mr.mf_pd.application.view.check.hf

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.mr.mf_pd.application.R
import com.mr.mf_pd.application.common.ConstantInt
import com.mr.mf_pd.application.common.ConstantStr
import com.mr.mf_pd.application.databinding.CheckHFDataBinding
import com.mr.mf_pd.application.utils.getViewModelFactory
import com.mr.mf_pd.application.view.base.BaseCheckActivity
import com.mr.mf_pd.application.view.base.BaseCheckFragment
import com.mr.mf_pd.application.view.callback.CheckActionListener
import com.mr.mf_pd.application.view.check.hf.setting.HFSettingActivity
import com.mr.mf_pd.application.view.fragment.phase.PhaseModelFragment
import com.mr.mf_pd.application.view.fragment.real.RealModelFragment
import kotlinx.android.synthetic.main.activity_check_hf.*

class CheckHFActivity : BaseCheckActivity<CheckHFDataBinding>() , CheckActionListener {


    private val viewModel by viewModels<CheckHFViewModel> { getViewModelFactory() }

    override fun initView(savedInstanceState: Bundle?) {
        titleList.add(mode1Tv)
        titleList.add(mode2Tv)
        super.initView(savedInstanceState)
    }

    override fun initData(savedInstanceState: Bundle?) {
        super.initData(savedInstanceState)
        viewModel.start(checkType)
    }

    override fun getContentView(): Int {
        return R.layout.activity_check_hf
    }

    override fun getViewPager(): ViewPager2 {
        return viewPager
    }

    override fun getScrollBlueBgView(): View? {
        return scrollBlueBgView
    }

    override fun settingClick() {
        val intent = Intent(this, HFSettingActivity::class.java)
        intent.putExtra(ConstantStr.KEY_BUNDLE_OBJECT,checkType)
        startActivity(intent)
    }

    override fun createCheckFragment(position: Int): BaseCheckFragment<*> {
        return if (position == 0) {
            PhaseModelFragment.create()
        } else {
            RealModelFragment.create()
        }
    }

    override fun addLimitValue() {
        if (viewModel.settingValues.size == checkType.settingLength && !viewModel.writeSetting) {
            val currentValue = viewModel.settingValues[getLimitPosition()].toInt()
            var newValue = currentValue + ConstantInt.LIMIT_VALUE_STEP
            if (newValue > 8192) {
                newValue = 8192
            }
            viewModel.settingValues[getLimitPosition()] = newValue.toFloat()
            writeSettingValue()
        }
    }

    override fun downLimitValue() {
        if (viewModel.settingValues.size == checkType.settingLength && !viewModel.writeSetting) {
            val currentValue = viewModel.settingValues[getLimitPosition()].toInt()
            var newValue = currentValue - ConstantInt.LIMIT_VALUE_STEP
            if (newValue < 0) {
                newValue = 0
            }
            viewModel.settingValues[getLimitPosition()] = newValue.toFloat()
            writeSettingValue()
        }
    }

    override fun getSettingValues(): List<Float> {
        return viewModel.settingValues
    }


    override fun getLimitPosition(): Int {
        return 7
    }

    override fun getBandDetectionPosition(): Int {
        return 8
    }

    override fun changeBandDetectionModel() {
        if (viewModel.settingValues.size == checkType.settingLength && !viewModel.writeSetting) {
            val currentModel = viewModel.settingValues[getBandDetectionPosition()].toInt()
            var newModel = currentModel + 1
            if (currentModel + 1 > 2) {
                newModel = 0
            }
            viewModel.settingValues[getBandDetectionPosition()] = newModel.toFloat()
            writeSettingValue()
        }
    }

    override fun writeSettingValue() {
        viewModel.writeSetting = true
        viewModel.writeValue()
    }

}