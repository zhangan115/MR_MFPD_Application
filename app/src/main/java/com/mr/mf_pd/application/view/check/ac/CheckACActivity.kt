package com.mr.mf_pd.application.view.check.ac

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.widget.PopupWindow
import android.widget.TextView
import androidx.activity.viewModels
import androidx.viewpager2.widget.ViewPager2
import com.mr.mf_pd.application.R
import com.mr.mf_pd.application.common.ConstantInt
import com.mr.mf_pd.application.common.ConstantStr
import com.mr.mf_pd.application.databinding.CheckACDataBinding
import com.mr.mf_pd.application.utils.getViewModelFactory
import com.mr.mf_pd.application.view.check.ac.flight.ACFlightModelFragment
import com.mr.mf_pd.application.view.check.ac.pulse.ACPulseModelFragment
import com.mr.mf_pd.application.view.check.ac.setting.ACSettingActivity
import com.mr.mf_pd.application.view.base.BaseCheckActivity
import com.mr.mf_pd.application.view.base.BaseCheckFragment
import com.mr.mf_pd.application.view.callback.CheckActionListener
import com.mr.mf_pd.application.view.fragment.continuity.ContinuityModelFragment
import com.mr.mf_pd.application.view.fragment.phase.PhaseModelFragment
import com.mr.mf_pd.application.view.fragment.real.RealModelFragment
import com.sito.tool.library.utils.DisplayUtil
import kotlinx.android.synthetic.main.activity_check_ac.*

class CheckACActivity : BaseCheckActivity<CheckACDataBinding>() , CheckActionListener {


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
        super.initData(savedInstanceState)
        viewModel.start(checkType)
    }

    override fun getContentView(): Int {
        return R.layout.activity_check_ac
    }

    override fun getViewPager(): ViewPager2 {
        return viewPager
    }

    override fun getScrollBlueBgView(): View? {
        return scrollBlueBgView
    }

    override fun settingClick() {
        val intent = Intent(this, ACSettingActivity::class.java)
        intent.putExtra(ConstantStr.KEY_BUNDLE_OBJECT,checkType)
        startActivity(intent)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        return false
    }

    override fun onResume() {
        super.onResume()
       viewModel.updateCallback()
    }

    override fun createCheckFragment(position: Int): BaseCheckFragment<*> {
        return when (position) {
            0 -> {
                ContinuityModelFragment.create(false)
            }
            1 -> {
                PhaseModelFragment.create(false)
            }
            2 -> {
                ACFlightModelFragment.create()
            }
            3 -> {
                RealModelFragment.create(false)
            }
            else -> {
                ACPulseModelFragment.create()
            }
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
        return -1
    }

    override fun changeBandDetectionModel() {

    }

    override fun writeSettingValue() {
        viewModel.writeSetting = true
        viewModel.writeValue()
    }

}