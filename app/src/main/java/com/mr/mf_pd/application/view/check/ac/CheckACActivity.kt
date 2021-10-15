package com.mr.mf_pd.application.view.check.ac

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.widget.PopupWindow
import android.widget.TextView
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.mr.mf_pd.application.R
import com.mr.mf_pd.application.databinding.CheckACDataBinding
import com.mr.mf_pd.application.utils.getViewModelFactory
import com.mr.mf_pd.application.view.check.tev.continuity.TEVContinuityFragment
import com.mr.mf_pd.application.view.check.ac.flight.ACFlightFragment
import com.mr.mf_pd.application.view.check.tev.phase.TEVPhaseModelFragment
import com.mr.mf_pd.application.view.check.ac.pulse.ACPulseFragment
import com.mr.mf_pd.application.view.check.tev.real.TEVRealFragment
import com.mr.mf_pd.application.view.check.ac.setting.ACSettingActivity
import com.mr.mf_pd.application.view.base.BaseCheckActivity
import com.sito.tool.library.utils.DisplayUtil
import kotlinx.android.synthetic.main.activity_check_ac.*

class CheckACActivity : BaseCheckActivity<CheckACDataBinding>() {


    private val viewModel by viewModels<CheckACViewModel> { getViewModelFactory() }

    override fun initView(savedInstanceState: Bundle?) {
        titleList.add(mode1Tv)
        titleList.add(mode2Tv)
        titleList.add(mode3Tv)
        titleList.add(mode4Tv)
        titleList.add(mode5Tv)
        menuMore.setOnClickListener {
            if (getViewPager().currentItem == 4) {
                showMcPopWindows(it)
            } else {
                showPopWindows(it)
            }
        }
        super.initView(savedInstanceState)
    }

    private fun showPopWindows(it: View?) {
        val layout = LayoutInflater.from(this).inflate(R.layout.layout_ac_pop_window, null)
        val popupWindow =
            PopupWindow(layout, DisplayUtil.dip2px(this, 115f), DisplayUtil.dip2px(this, 90f))
        popupWindow.isOutsideTouchable = true
        popupWindow.isTouchable = true
        layout.findViewById<TextView>(R.id.savePath).setOnClickListener {
            popupWindow.dismiss()
        }
        layout.findViewById<TextView>(R.id.setting).setOnClickListener {
            popupWindow.dismiss()
            settingClick()
        }
        popupWindow.showAsDropDown(it)
    }

    private fun showMcPopWindows(it: View?) {
        val layout = LayoutInflater.from(this).inflate(R.layout.layout_ac_mc_pop_window, null)
        val popupWindow =
            PopupWindow(layout, DisplayUtil.dip2px(this, 115f), DisplayUtil.dip2px(this, 135f))
        popupWindow.isOutsideTouchable = true
        popupWindow.isTouchable = true
        layout.findViewById<TextView>(R.id.savePath).setOnClickListener {
            popupWindow.dismiss()
        }
        layout.findViewById<TextView>(R.id.mc).setOnClickListener {
            popupWindow.dismiss()
        }
        layout.findViewById<TextView>(R.id.setting).setOnClickListener {
            popupWindow.dismiss()
            settingClick()
        }
        popupWindow.showAsDropDown(it)
    }

    override fun initData(savedInstanceState: Bundle?) {
        fragmentCount = 5
        super.initData(savedInstanceState)

    }

    override fun getContentView(): Int {
        return R.layout.activity_check_ac
    }

    override fun getToolBarTitle(): String {
        return "超声波形（AC）"
    }

    override fun getViewPager(): ViewPager2 {
        return viewPager
    }

    override fun getScrollBlueBgView(): View? {
        return scrollBlueBgView
    }

    override fun settingClick() {
        val intent = Intent(this, ACSettingActivity::class.java)
        startActivity(intent)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        return false
    }

    override fun createCheckFragment(position: Int): Fragment {
        return if (position == 0) {
            TEVContinuityFragment.create(mDeviceBean)
        } else if (position == 1) {
            TEVPhaseModelFragment.create(mDeviceBean)
        } else if (position == 2) {
            ACFlightFragment.create(mDeviceBean)
        } else if (position == 3) {
            TEVRealFragment.create(mDeviceBean)
        } else {
            ACPulseFragment.create(mDeviceBean)
        }
    }

}