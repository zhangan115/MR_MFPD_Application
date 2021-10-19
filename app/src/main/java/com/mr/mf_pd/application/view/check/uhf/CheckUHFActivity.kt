package com.mr.mf_pd.application.view.check.uhf

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.mr.mf_pd.application.R
import com.mr.mf_pd.application.databinding.CheckUHFDataBinding
import com.mr.mf_pd.application.utils.getViewModelFactory
import com.mr.mf_pd.application.view.base.BaseCheckActivity
import com.mr.mf_pd.application.view.check.uhf.phase.UHFPhaseModelFragment
import com.mr.mf_pd.application.view.check.uhf.real.UHFRealModelFragment
import com.mr.mf_pd.application.view.check.uhf.setting.UHFSettingActivity
import kotlinx.android.synthetic.main.activity_check_uhf.*

class CheckUHFActivity : BaseCheckActivity<CheckUHFDataBinding>() {

    private val viewModel by viewModels<CheckUHFViewModel> { getViewModelFactory() }

    override fun initView(savedInstanceState: Bundle?) {
        titleList.add(mode1Tv)
        titleList.add(mode2Tv)
        super.initView(savedInstanceState)
    }

    override fun initData(savedInstanceState: Bundle?) {
        fragmentCount = 2
        super.initData(savedInstanceState)
        viewModel.start()
    }

    override fun getContentView(): Int {
        return R.layout.activity_check_uhf
    }

    override fun getToolBarTitle(): String {
        return "特高频（UHF）"
    }

    override fun getViewPager(): ViewPager2 {
        return viewPager
    }

    override fun getScrollBlueBgView(): View? {
        return scrollBlueBgView
    }

    override fun settingClick() {
        val intent = Intent(this, UHFSettingActivity::class.java)
        startActivity(intent)
    }

    override fun createCheckFragment(position: Int): Fragment {
        return if (position == 0) {
            UHFPhaseModelFragment.create(mDeviceBean)
        } else {
            UHFRealModelFragment.create(mDeviceBean)
        }
    }

}