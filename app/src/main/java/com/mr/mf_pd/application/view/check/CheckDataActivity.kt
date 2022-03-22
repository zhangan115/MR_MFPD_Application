package com.mr.mf_pd.application.view.check

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.viewModels
import androidx.viewpager2.widget.ViewPager2
import com.google.gson.Gson
import com.mr.mf_pd.application.R
import com.mr.mf_pd.application.common.CheckType
import com.mr.mf_pd.application.common.ConstantInt
import com.mr.mf_pd.application.common.ConstantStr
import com.mr.mf_pd.application.common.Constants
import com.mr.mf_pd.application.databinding.FileDataDataBinding
import com.mr.mf_pd.application.model.EventObserver
import com.mr.mf_pd.application.model.SettingBean
import com.mr.mf_pd.application.utils.getViewModelFactory
import com.mr.mf_pd.application.view.base.BaseCheckActivity
import com.mr.mf_pd.application.view.base.BaseCheckFragment
import com.mr.mf_pd.application.view.callback.CheckActivityListener
import com.mr.mf_pd.application.view.callback.FragmentDataListener
import com.mr.mf_pd.application.view.check.flight.ACFlightModelFragment
import com.mr.mf_pd.application.view.check.pulse.ACPulseModelFragment
import com.mr.mf_pd.application.view.setting.ae.AESettingActivity
import com.mr.mf_pd.application.view.setting.hf.HFSettingActivity
import com.mr.mf_pd.application.view.setting.tev.TEVSettingActivity
import com.mr.mf_pd.application.view.setting.uhf.UHFSettingActivity
import com.mr.mf_pd.application.view.check.continuity.ContinuityModelFragment
import com.mr.mf_pd.application.view.check.phase.PhaseModelFragment
import com.mr.mf_pd.application.view.check.real.RealModelFragment
import com.sito.tool.library.utils.SPHelper
import kotlinx.android.synthetic.main.activity_file_data.*

class CheckDataActivity : BaseCheckActivity<FileDataDataBinding>(), View.OnClickListener,
    CheckActivityListener {

    private val viewModel by viewModels<CheckDataViewModel> { getViewModelFactory() }

    private var clickClass: Class<*>? = null
    private var limitPosition: Int = -1
    private var bandDetectionPosition: Int = -1
    private var fragmentDataListener: ArrayList<FragmentDataListener> = ArrayList()

    override fun initView(savedInstanceState: Bundle?) {
        when (checkType) {
            CheckType.UHF -> {
                checkFragmentLayout.addView(createTitleTextView("相位模式", "0"))
                checkFragmentLayout.addView(createTitleTextView("实时模式", "1"))
                fragments.add(PhaseModelFragment.create(false))
                fragments.add(RealModelFragment.create(false))
                clickClass = UHFSettingActivity::class.java
                limitPosition = 7
                bandDetectionPosition = 8
            }
            CheckType.HF -> {
                checkFragmentLayout.addView(createTitleTextView("相位模式", "0"))
                checkFragmentLayout.addView(createTitleTextView("实时模式", "1"))
                fragments.add(PhaseModelFragment.create(false))
                fragments.add(RealModelFragment.create(false))
                clickClass = HFSettingActivity::class.java
                limitPosition = 7
                bandDetectionPosition = 8
            }
            CheckType.TEV -> {
                checkFragmentLayout.addView(createTitleTextView("连续模式", "0"))
                checkFragmentLayout.addView(createTitleTextView("相位模式", "1"))
                checkFragmentLayout.addView(createTitleTextView("实时模式", "2"))
                fragments.add(ContinuityModelFragment.create(false))
                fragments.add(PhaseModelFragment.create(false))
                fragments.add(RealModelFragment.create(false))
                clickClass = TEVSettingActivity::class.java
                limitPosition = 7
                bandDetectionPosition = -1
            }
            CheckType.AE -> {
                checkFragmentLayout.addView(createTitleTextView("连续模式", "0"))
                checkFragmentLayout.addView(createTitleTextView("相位模式", "1"))
                checkFragmentLayout.addView(createTitleTextView("飞行模式", "2"))
                checkFragmentLayout.addView(createTitleTextView("实时模式", "3"))
                checkFragmentLayout.addView(createTitleTextView("脉冲波形", "4"))
                fragments.add(ContinuityModelFragment.create(false))
                fragments.add(PhaseModelFragment.create(false))
                fragments.add(ACFlightModelFragment.create())
                fragments.add(RealModelFragment.create(false))
                fragments.add(ACPulseModelFragment.create())
                clickClass = AESettingActivity::class.java
                limitPosition = 7
                bandDetectionPosition = -1
            }
        }
        super.initView(savedInstanceState)
        fragments.forEach {
            fragmentDataListener.add(it)
        }
    }

    override fun initData(savedInstanceState: Bundle?) {
        super.initData(savedInstanceState)
        viewModel.start(checkType)
        viewModel.toYcDataEvent.observe(this, EventObserver {
            Log.d("zhangan", Thread.currentThread().name)
            runOnUiThread {
                fragmentDataListener.forEach { listener ->
                    listener.onYcDataChange(it)
                }
            }
        })
    }

    override fun onClick(v: View?) {
        val tag: Int = Integer.valueOf(v?.tag as String)
        if (currentIndex == tag) {
            return
        }
        if (fragments.isNotEmpty() && fragments[currentIndex].isSaving()) {
            fragments[currentIndex].showToSaveDialog {
                currentIndex = tag
                viewPager.setCurrentItem(tag, false)
            }
        } else {
            currentIndex = tag
            viewPager.setCurrentItem(tag, false)
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.updateCallback()
    }

    override fun getContentView(): Int {
        return R.layout.activity_file_data
    }


    override fun getViewPager(): ViewPager2 {
        return viewPager
    }

    override fun getScrollBlueBgView(): View? {
        return scrollBlueBgView
    }

    override fun settingClick() {
        val intent = Intent(this, clickClass)
        intent.putExtra(ConstantStr.KEY_BUNDLE_OBJECT, checkType)
        startActivity(intent)
    }

    override fun createCheckFragment(position: Int): BaseCheckFragment<*> {
        return fragments[position]
    }

    override fun getSettingValues(): List<Float> {
        return viewModel.settingValues
    }

    override fun writeSettingValue() {
        viewModel.writeSetting = true
        viewModel.writeValue()
    }

    override fun getLimitPosition(): Int {
        return limitPosition
    }

    override fun getBandDetectionPosition(): Int {
        return bandDetectionPosition
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

    private fun createTitleTextView(title: String, tag: String): TextView {
        val textView = TextView(this)
        textView.text = title
        textView.setTextColor(findColor(R.color.text_title))
        textView.textSize = 15f
        textView.tag = tag
        textView.gravity = Gravity.CENTER
        textView.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT)
        titleList.add(textView)
        return textView
    }


}