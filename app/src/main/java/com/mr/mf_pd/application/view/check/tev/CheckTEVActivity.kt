package com.mr.mf_pd.application.view.check.tev

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.mr.mf_pd.application.R
import com.mr.mf_pd.application.common.ConstantInt
import com.mr.mf_pd.application.common.ConstantStr
import com.mr.mf_pd.application.databinding.CheckTEVDataBinding
import com.mr.mf_pd.application.utils.getViewModelFactory
import com.mr.mf_pd.application.view.base.BaseCheckActivity
import com.mr.mf_pd.application.view.base.BaseCheckFragment
import com.mr.mf_pd.application.view.callback.CheckActionListener
import com.mr.mf_pd.application.view.fragment.continuity.ContinuityModelFragment
import com.mr.mf_pd.application.view.check.tev.setting.TEVSettingActivity
import com.mr.mf_pd.application.view.fragment.phase.PhaseModelFragment
import com.mr.mf_pd.application.view.fragment.real.RealModelFragment
import kotlinx.android.synthetic.main.activity_check_tev.*

class CheckTEVActivity : BaseCheckActivity<CheckTEVDataBinding>() , CheckActionListener {


    private val viewModel by viewModels<CheckTEVViewModel> { getViewModelFactory() }

    override fun initView(savedInstanceState: Bundle?) {
        titleList.add(mode1Tv)
        titleList.add(mode2Tv)
        titleList.add(mode3Tv)
        super.initView(savedInstanceState)
    }

    override fun initData(savedInstanceState: Bundle?) {
        fragmentCount = 3
        super.initData(savedInstanceState)
        viewModel.start(checkType)
    }

    override fun getContentView(): Int {
        return R.layout.activity_check_tev
    }

    override fun getViewPager(): ViewPager2 {
        return viewPager
    }

    override fun getScrollBlueBgView(): View? {
        return scrollBlueBgView
    }

    override fun settingClick() {
        val intent = Intent(this, TEVSettingActivity::class.java)
        intent.putExtra(ConstantStr.KEY_BUNDLE_OBJECT, checkType)
        startActivity(intent)
    }

    override fun createCheckFragment(position: Int): BaseCheckFragment<*> {
        return when (position) {
            0 -> {
                ContinuityModelFragment.create(mDeviceBean)
            }
            1 -> {
                PhaseModelFragment.create(mDeviceBean)
            }
            else -> {
                RealModelFragment.create(mDeviceBean)
            }
        }
    }

    override fun getSettingValues(): List<Float> {
        return viewModel.settingValues
    }

    override fun writeSettingValue() {
        viewModel.writeSetting = true
        viewModel.writeValue()
    }

    override fun getLimitPosition(): Int {
        return 7
    }

    override fun getBandDetectionPosition(): Int {
        return -1
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

    override fun changeBandDetectionModel() {

    }

    override fun getYcByteArray(): ByteArray? {
        return viewModel.ycByteArray
    }

}