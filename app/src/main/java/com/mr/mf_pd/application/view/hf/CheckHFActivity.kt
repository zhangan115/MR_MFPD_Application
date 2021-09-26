package com.mr.mf_pd.application.view.hf

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.mr.mf_pd.application.R
import com.mr.mf_pd.application.databinding.CheckHFDataBinding
import com.mr.mf_pd.application.utils.getViewModelFactory
import com.mr.mf_pd.application.view.tev.phase.TEVPhaseModelFragment
import com.mr.mf_pd.application.view.base.BaseCheckActivity
import com.mr.mf_pd.application.view.tev.real.TEVRealFragment
import com.mr.mf_pd.application.view.hf.setting.HFSettingActivity
import kotlinx.android.synthetic.main.activity_check_hf.*

class CheckHFActivity : BaseCheckActivity<CheckHFDataBinding>() {


    private val viewModel by viewModels<CheckHFViewModel> { getViewModelFactory() }

    override fun initView(savedInstanceState: Bundle?) {
        titleList.add(mode1Tv)
        titleList.add(mode2Tv)
        super.initView(savedInstanceState)
    }

    override fun initData(savedInstanceState: Bundle?) {
        fragmentCount = 2
        super.initData(savedInstanceState)
    }

    override fun getContentView(): Int {
        return R.layout.activity_check_hf
    }

    override fun getToolBarTitle(): String {
        return "高频（HF）"
    }

    override fun getViewPager(): ViewPager2 {
        return viewPager
    }

    override fun getScrollBlueBgView(): View? {
        return scrollBlueBgView
    }

    override fun settingClick() {
        val intent = Intent(this, HFSettingActivity::class.java)
        startActivity(intent)
    }

    override fun createCheckFragment(position: Int): Fragment {
        return if (position == 0) {
            TEVPhaseModelFragment.create(mDeviceBean)
        } else {
            TEVRealFragment.create(mDeviceBean)
        }
    }

}