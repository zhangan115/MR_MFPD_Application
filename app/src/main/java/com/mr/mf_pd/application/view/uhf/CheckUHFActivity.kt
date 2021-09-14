package com.mr.mf_pd.application.view.uhf

import android.content.Intent
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.mr.mf_pd.application.R
import com.mr.mf_pd.application.common.ConstantStr
import com.mr.mf_pd.application.databinding.CheckUHFDataBinding
import com.mr.mf_pd.application.model.DeviceBean
import com.mr.mf_pd.application.utils.getViewModelFactory
import com.mr.mf_pd.application.view.base.AbsBaseActivity
import com.mr.mf_pd.application.view.uhf.phase.UHFPhaseModelFragment
import com.mr.mf_pd.application.view.uhf.real.UHFRealModelFragment
import com.mr.mf_pd.application.view.uhf.setting.UHFSettingActivity
import com.sito.tool.library.utils.DisplayUtil
import kotlinx.android.synthetic.main.activity_check_uhf.*

class CheckUHFActivity : AbsBaseActivity<CheckUHFDataBinding>() {

    private val viewModel by viewModels<CheckUHFViewModel> { getViewModelFactory() }

    private var mDeviceBean: DeviceBean? = null

    override fun initView(savedInstanceState: Bundle?) {
        val width =
            ((resources.displayMetrics.widthPixels - DisplayUtil.dip2px(this, 24f)) / 2)
        mode1Tv.layoutParams =
            LinearLayout.LayoutParams(width, LinearLayout.LayoutParams.MATCH_PARENT)
        mode2Tv.layoutParams =
            LinearLayout.LayoutParams(width, LinearLayout.LayoutParams.MATCH_PARENT)

        mode1Tv.setOnClickListener {
            viewPager.setCurrentItem(0, false)
        }
        mode2Tv.setOnClickListener {
            viewPager.setCurrentItem(1, false)
        }

        scrollBlueBgView.layoutParams =
            RelativeLayout.LayoutParams(width, RelativeLayout.LayoutParams.MATCH_PARENT)
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                val layoutParams = RelativeLayout.LayoutParams(
                    scrollBlueBgView.layoutParams.width,
                    scrollBlueBgView.layoutParams.height
                )
                layoutParams.marginStart = ((position + positionOffset) * width).toInt()
                scrollBlueBgView.layoutParams = layoutParams
            }

            override fun onPageSelected(position: Int) {
                if (position == 0) {
                    mode1Tv.setTypeface(null, Typeface.BOLD)
                    mode2Tv.setTypeface(null, Typeface.NORMAL)
                    mode1Tv.setTextColor(findColor(R.color.colorWhite))
                    mode2Tv.setTextColor(findColor(R.color.text_un_select))
                } else {
                    mode2Tv.setTextColor(findColor(R.color.colorWhite))
                    mode1Tv.setTextColor(findColor(R.color.text_un_select))
                    mode1Tv.setTypeface(null, Typeface.NORMAL)
                    mode2Tv.setTypeface(null, Typeface.BOLD)
                }
            }
        })
        viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun createFragment(position: Int): Fragment {
                return if (position == 0) {
                    UHFPhaseModelFragment.create(mDeviceBean)
                } else {
                    UHFRealModelFragment.create(mDeviceBean)
                }
            }

            override fun getItemCount(): Int {
                return 2
            }
        }
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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_setting, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu_setting) {
            val intent = Intent(this, UHFSettingActivity::class.java)
            startActivity(intent)
        }
        return true
    }

}