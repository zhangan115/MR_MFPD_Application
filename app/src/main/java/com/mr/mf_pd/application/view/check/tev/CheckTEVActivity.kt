package com.mr.mf_pd.application.view.check.tev

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.mr.mf_pd.application.R
import com.mr.mf_pd.application.databinding.CheckTEVDataBinding
import com.mr.mf_pd.application.utils.getViewModelFactory
import com.mr.mf_pd.application.view.base.BaseCheckActivity
import com.mr.mf_pd.application.view.check.tev.continuity.TEVContinuityFragment
import com.mr.mf_pd.application.view.check.tev.phase.TEVPhaseModelFragment
import com.mr.mf_pd.application.view.check.tev.real.TEVRealFragment
import com.mr.mf_pd.application.view.check.tev.setting.TEVSettingActivity
import kotlinx.android.synthetic.main.activity_check_tev.*

class CheckTEVActivity : BaseCheckActivity<CheckTEVDataBinding>() {


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
    }

    override fun getContentView(): Int {
        return R.layout.activity_check_tev
    }

    override fun getToolBarTitle(): String {
        return "暂态地电压（TEV）"
    }

    override fun getViewPager(): ViewPager2 {
        return viewPager
    }

    override fun getScrollBlueBgView(): View? {
        return scrollBlueBgView
    }

    override fun settingClick() {
        val intent = Intent(this, TEVSettingActivity::class.java)
        startActivity(intent)
    }

    override fun createCheckFragment(position: Int): Fragment {
        return if (position == 0) {
            TEVContinuityFragment.create(mDeviceBean)
        } else if (position == 1) {
            TEVRealFragment.create(mDeviceBean)
        }else{
            TEVRealFragment.create(mDeviceBean)
        }
    }

}