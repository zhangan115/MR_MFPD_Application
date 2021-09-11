package com.mr.mf_pd.application.view.uhf

import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.util.LogTime
import com.mr.mf_pd.application.R
import com.mr.mf_pd.application.common.ConstantStr
import com.mr.mf_pd.application.databinding.CheckUHFDataBinding
import com.mr.mf_pd.application.model.DeviceBean
import com.mr.mf_pd.application.utils.getViewModelFactory
import com.mr.mf_pd.application.view.base.AbsBaseActivity
import com.mr.mf_pd.application.view.main.MainViewModel
import com.mr.mf_pd.application.view.uhf.prpd.UHFPrPdFragment
import com.mr.mf_pd.application.view.uhf.prps.UHFPrPsFragment
import com.sito.tool.library.utils.DisplayUtil
import kotlinx.android.synthetic.main.activity_check_uhf.*

class CheckUHFActivity : AbsBaseActivity<CheckUHFDataBinding>() {

    private val viewModel by viewModels<CheckUHFViewModel> { getViewModelFactory() }

    private var mDeviceBean: DeviceBean? = null

    override fun initView(savedInstanceState: Bundle?) {
        val width =
            ((resources.displayMetrics.widthPixels - DisplayUtil.dip2px(this, 24f)) / 2).toInt()
        mode1Tv.layoutParams =
            LinearLayout.LayoutParams(width, LinearLayout.LayoutParams.MATCH_PARENT)
        mode2Tv.layoutParams =
            LinearLayout.LayoutParams(width, LinearLayout.LayoutParams.MATCH_PARENT)

        mode1Tv.setOnClickListener {
            viewPager.setCurrentItem(0, true)
        }
        mode2Tv.setOnClickListener {
            viewPager.setCurrentItem(1, true)
        }

        scrollBlueBgView.layoutParams =
            FrameLayout.LayoutParams(width, FrameLayout.LayoutParams.MATCH_PARENT)
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                Log.d(
                    "za",
                    "position  is $position positionOffset is $positionOffset positionOffsetPixels is $positionOffsetPixels"
                )
                val scrollX: Int = position * width + width * positionOffset.toInt()
                scrollView.smoothScrollTo(scrollX, 0)
            }

            override fun onPageSelected(position: Int) {
                viewModel.currentIndex.postValue(position)
                if (position == 0) {
                    mode1Tv.setTypeface(null, Typeface.BOLD)
                    mode2Tv.setTypeface(null, Typeface.NORMAL)
                    val layoutParams = FrameLayout.LayoutParams(
                        scrollBlueBgView.layoutParams.width,
                        scrollBlueBgView.layoutParams.height
                    )
                    layoutParams.marginStart = 0
                    scrollBlueBgView.layoutParams = layoutParams
                } else {
                    mode1Tv.setTypeface(null, Typeface.NORMAL)
                    mode2Tv.setTypeface(null, Typeface.BOLD)
                    val layoutParams = FrameLayout.LayoutParams(
                        scrollBlueBgView.layoutParams.width,
                        scrollBlueBgView.layoutParams.height
                    )
                    layoutParams.marginStart = width
                    scrollBlueBgView.layoutParams = layoutParams
                }
            }
        })
        viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun createFragment(position: Int): Fragment {
                return if (position == 0) {
                    UHFPrPdFragment.create(mDeviceBean)
                } else {
                    UHFPrPsFragment.create(mDeviceBean)
                }
            }

            override fun getItemCount(): Int {
                return 2
            }
        }
        mode1Tv.setTextColor(findColor(R.color.colorWhite))
    }

    override fun initData(savedInstanceState: Bundle?) {
        mDeviceBean = intent.getParcelableExtra(ConstantStr.KEY_BUNDLE_OBJECT)
    }

    override fun getContentView(): Int {
        return R.layout.activity_check_uhf
    }

    override fun getToolBarTitle(): String {
        return "特高频（UHF）"
    }
}