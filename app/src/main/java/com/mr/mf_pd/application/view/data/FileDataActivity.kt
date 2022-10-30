package com.mr.mf_pd.application.view.data

import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.mr.mf_pd.application.R
import com.mr.mf_pd.application.common.CheckType
import com.mr.mf_pd.application.common.ConstantStr
import com.mr.mf_pd.application.databinding.FileDataDataBinding
import com.mr.mf_pd.application.manager.file.CheckFileReadManager
import com.mr.mf_pd.application.model.EventObserver
import com.mr.mf_pd.application.utils.*
import com.mr.mf_pd.application.view.base.AbsBaseActivity
import com.mr.mf_pd.application.view.base.BaseCheckFragment
import com.mr.mf_pd.application.view.callback.FragmentDataListener
import com.mr.mf_pd.application.view.check.flight.ACFlightModelFragment
import com.mr.mf_pd.application.view.check.pulse.ACPulseModelFragment
import com.mr.mf_pd.application.view.file.model.CheckDataFileModel
import com.mr.mf_pd.application.view.check.continuity.ContinuityModelFragment
import com.mr.mf_pd.application.view.check.phase.PhaseModelChartFragment
import com.mr.mf_pd.application.view.check.real.RealModelFragment
import com.sito.tool.library.utils.DisplayUtil
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_file_data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File

class FileDataActivity : AbsBaseActivity<FileDataDataBinding>(), View.OnClickListener {

    private val viewModel by viewModels<FileDataViewModel> { getViewModelFactory() }

    lateinit var currentFile: File
    lateinit var checkType: CheckType
    private var toolbarTitleStr = ""
    var currentIndex = 0
    var titleList: ArrayList<TextView> = ArrayList()
    var fragments: ArrayList<BaseCheckFragment<*>> = ArrayList()
    var width: Int? = null

    //一秒修改一次页面的定时器
    private var mOneSecondDisposable: Disposable? = null
    private var fragmentDataListener: ArrayList<FragmentDataListener> = ArrayList()

    override fun initView(savedInstanceState: Bundle?) {

    }

    override fun initData(savedInstanceState: Bundle?) {
        CheckFileReadManager.get().initQueue()
        intent.getStringExtra(ConstantStr.KEY_BUNDLE_STR)?.let {
            currentFile = File(it)
            GlobalScope.runCatching {
                val checkDataFileModel = FileUtils.isCheckDataFile(currentFile,null)
                GlobalScope.launch(Dispatchers.Main) {
                    if (checkDataFileModel != null) {
                        initViewData(checkDataFileModel)
                    } else {
                        throw RuntimeException("文件不合法，无法读取")
                    }
                }
            }
        }
    }

    private fun initViewData(checkDataFileModel: CheckDataFileModel) {
        viewModel.fileRepository.setCheckFileModel(checkDataFileModel)
        viewModel.toYcDataEvent.observe(this, EventObserver {
            runOnUiThread {
                fragmentDataListener.forEach { listener ->
                    if (listener.isAdd())
                        listener.onYcDataChange(it)
                }
            }
        })
        viewModel.toFdDataEvent.observe(this, EventObserver {
            fragmentDataListener.forEach { listener ->
                if (listener.isAdd()) {
                    listener.onFdDataChange(it)
                }
            }
        })
        val time = DateUtil.date2TimeStamp(currentFile.name, "yyyy_MM_dd_HH_mm_ss")
        val titleName = DateUtil.timeFormat(time, null)
        setTitleValue(titleName + findString(checkDataFileModel.fileType!!.description))
        checkType = FileTypeUtils.getCheckType(checkDataFileModel.fileType)!!
        when (checkType) {
            CheckType.UHF -> {
                checkFragmentLayout.addView(createTitleTextView("相位模式", "0"))
                checkFragmentLayout.addView(createTitleTextView("实时模式", "1"))
                fragments.add(PhaseModelChartFragment.create(true))
                fragments.add(RealModelFragment.create(true))
            }
            CheckType.HF -> {
                checkFragmentLayout.addView(createTitleTextView("相位模式", "0"))
                checkFragmentLayout.addView(createTitleTextView("实时模式", "1"))
                fragments.add(PhaseModelChartFragment.create(true))
                fragments.add(RealModelFragment.create(true))
            }
            CheckType.TEV -> {
                checkFragmentLayout.addView(createTitleTextView("连续模式", "0"))
                checkFragmentLayout.addView(createTitleTextView("相位模式", "1"))
                checkFragmentLayout.addView(createTitleTextView("实时模式", "2"))
                fragments.add(ContinuityModelFragment.create(true))
                fragments.add(PhaseModelChartFragment.create(true))
                fragments.add(RealModelFragment.create(true))
            }
            CheckType.AE -> {
                checkFragmentLayout.addView(createTitleTextView("连续模式", "0"))
                checkFragmentLayout.addView(createTitleTextView("相位模式", "1"))
                checkFragmentLayout.addView(createTitleTextView("飞行模式", "2"))
                checkFragmentLayout.addView(createTitleTextView("实时模式", "3"))
                fragments.add(ContinuityModelFragment.create(true))
                fragments.add(PhaseModelChartFragment.create(true))
                fragments.add(ACFlightModelFragment.create(true))
                fragments.add(RealModelFragment.create(true))
            }
            CheckType.AA -> {
                checkFragmentLayout.addView(createTitleTextView("连续模式", "0"))
                checkFragmentLayout.addView(createTitleTextView("相位模式", "1"))
                checkFragmentLayout.addView(createTitleTextView("实时模式", "2"))
                fragments.add(ContinuityModelFragment.create(true))
                fragments.add(PhaseModelChartFragment.create(true))
                fragments.add(RealModelFragment.create(true))
            }
        }
        fragments.forEach {
            fragmentDataListener.add(it)
        }
        initFragmentView()
        viewModel.start(checkType, currentFile)
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

    private fun initFragmentView() {
        width =
            ((resources.displayMetrics.widthPixels - DisplayUtil.dip2px(this,
                24f)) / fragments.size)
        if (width != null) {
            titleList.forEach {
                it.layoutParams =
                    LinearLayout.LayoutParams(width!!, LinearLayout.LayoutParams.MATCH_PARENT)
                it.setOnClickListener(this)
            }
            scrollBlueBgView.layoutParams =
                RelativeLayout.LayoutParams(width!!, RelativeLayout.LayoutParams.MATCH_PARENT)
        }
        viewPager.isUserInputEnabled = false
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int,
            ) {
                if (scrollBlueBgView != null && width != null) {
                    val layoutParams = RelativeLayout.LayoutParams(
                        scrollBlueBgView.layoutParams.width,
                        scrollBlueBgView.layoutParams.height
                    )
                    layoutParams.marginStart = ((position + positionOffset) * width!!).toInt()
                    scrollBlueBgView.layoutParams = layoutParams
                }
            }

            override fun onPageSelected(position: Int) {
                for (textView in titleList) {
                    val tag: Int = Integer.valueOf(textView.tag as String)
                    if (position == tag) {
                        textView.setTypeface(null, Typeface.BOLD)
                        textView.setTextColor(findColor(R.color.colorWhite))
                    } else {
                        textView.setTypeface(null, Typeface.NORMAL)
                        textView.setTextColor(findColor(R.color.text_un_select))
                    }
                }
            }
        })
        viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun createFragment(position: Int): Fragment {
                return fragments[position]
            }

            override fun getItemCount(): Int {
                return fragments.size
            }
        }
    }

    override fun onResume() {
        super.onResume()
        mOneSecondDisposable?.dispose()
        mOneSecondDisposable = RepeatActionUtils.execute {
            runOnUiThread {
                fragmentDataListener.forEach {
                    it.onOneSecondUiChange()
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        mOneSecondDisposable?.dispose()
    }

    override fun onClick(v: View?) {
        val tag: Int = Integer.valueOf(v?.tag as String)
        if (currentIndex == tag) {
            return
        }
        if (fragments.isNotEmpty() && fragments[currentIndex].isSaving()) {
            fragments[currentIndex].showToSaveDialog {
                currentIndex = tag
                viewPager.setCurrentItem(tag, true)
            }
        } else {
            currentIndex = tag
            viewPager.setCurrentItem(tag, true)
        }
    }

    override fun getContentView(): Int {
        return R.layout.activity_file_data
    }

    override fun getToolBarTitle(): String {
        return toolbarTitleStr
    }

}