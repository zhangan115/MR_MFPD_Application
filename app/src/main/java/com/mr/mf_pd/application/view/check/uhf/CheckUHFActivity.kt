package com.mr.mf_pd.application.view.check.uhf

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.viewpager2.widget.ViewPager2
import com.mr.mf_pd.application.R
import com.mr.mf_pd.application.common.ConstantStr
import com.mr.mf_pd.application.databinding.CheckUHFDataBinding
import com.mr.mf_pd.application.manager.socket.CommandHelp
import com.mr.mf_pd.application.manager.socket.CommandType
import com.mr.mf_pd.application.manager.socket.SocketManager
import com.mr.mf_pd.application.utils.getViewModelFactory
import com.mr.mf_pd.application.view.base.BaseCheckActivity
import com.mr.mf_pd.application.view.base.BaseCheckFragment
import com.mr.mf_pd.application.view.fragment.phase.PhaseModelFragment
import com.mr.mf_pd.application.view.fragment.real.RealModelFragment
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
        viewModel.start(checkType)
    }

    override fun getContentView(): Int {
        return R.layout.activity_check_uhf
    }

    override fun getViewPager(): ViewPager2 {
        return viewPager
    }

    override fun getScrollBlueBgView(): View? {
        return scrollBlueBgView
    }

    override fun settingClick() {
        val intent = Intent(this, UHFSettingActivity::class.java)
        intent.putExtra(ConstantStr.KEY_BUNDLE_OBJECT, checkType)
        startActivity(intent)
    }

    override fun createCheckFragment(position: Int): BaseCheckFragment<*> {
        return if (position == 0) {
            PhaseModelFragment.create(mDeviceBean)
        } else {
            RealModelFragment.create(mDeviceBean)
        }
    }

}