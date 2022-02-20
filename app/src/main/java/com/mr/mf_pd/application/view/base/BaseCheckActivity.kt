package com.mr.mf_pd.application.view.base

import android.graphics.Typeface
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.mr.mf_pd.application.R
import com.mr.mf_pd.application.common.CheckType
import com.mr.mf_pd.application.common.ConstantStr
import com.mr.mf_pd.application.model.DeviceBean
import com.mr.mf_pd.application.view.callback.CheckActionListener
import com.sito.tool.library.utils.DisplayUtil

abstract class BaseCheckActivity<T : ViewDataBinding> : AbsBaseActivity<T>(), View.OnClickListener{

    open var mDeviceBean: DeviceBean? = null
    open var width: Int? = null
    open lateinit var checkType: CheckType
    open var fragmentCount = 2
    open var currentIndex = 0
    open var titleList: ArrayList<TextView> = ArrayList()
    open var fragments: ArrayList<BaseCheckFragment<*>> = ArrayList()

    override fun initView(savedInstanceState: Bundle?) {
        width =
            ((resources.displayMetrics.widthPixels - DisplayUtil.dip2px(this, 24f)) / fragmentCount)
        if (width != null) {
            titleList.forEach {
                it.layoutParams =
                    LinearLayout.LayoutParams(width!!, LinearLayout.LayoutParams.MATCH_PARENT)
                it.setOnClickListener(this)
            }
            getScrollBlueBgView()?.layoutParams =
                RelativeLayout.LayoutParams(width!!, RelativeLayout.LayoutParams.MATCH_PARENT)
        }
        getViewPager().isUserInputEnabled = false
        getViewPager().registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int,
            ) {
                if (getScrollBlueBgView() != null && width != null) {
                    val layoutParams = RelativeLayout.LayoutParams(
                        getScrollBlueBgView()!!.layoutParams.width,
                        getScrollBlueBgView()!!.layoutParams.height
                    )
                    layoutParams.marginStart = ((position + positionOffset) * width!!).toInt()
                    getScrollBlueBgView()!!.layoutParams = layoutParams
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
        getViewPager().adapter = object : FragmentStateAdapter(this) {
            override fun createFragment(position: Int): Fragment {
                val fragment = createCheckFragment(position)
                fragments.add(fragment)
                return fragment
            }

            override fun getItemCount(): Int {
                return fragmentCount
            }
        }
    }

    override fun initData(savedInstanceState: Bundle?) {
        checkType = intent.getSerializableExtra(ConstantStr.KEY_BUNDLE_OBJECT) as CheckType
        mDeviceBean = intent.getParcelableExtra(ConstantStr.KEY_BUNDLE_OBJECT_1)
    }

    override fun getToolBarTitle(): String {
        return findString(checkType.description)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_setting, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu_setting) {
            settingClick()
        }
        return true
    }

    abstract fun getViewPager(): ViewPager2

    abstract fun getScrollBlueBgView(): View?

    abstract fun settingClick()

    abstract fun createCheckFragment(position: Int): BaseCheckFragment<*>

    override fun onClick(v: View?) {
        val tag: Int = Integer.valueOf(v?.tag as String)
        if (currentIndex == tag) {
            return
        }
        if (fragments.isNotEmpty() && fragments[currentIndex].isSaving()) {
            fragments[currentIndex].showToSaveDialog {
                currentIndex = tag
                getViewPager().setCurrentItem(tag, true)
            }
        } else {
            currentIndex = tag
            getViewPager().setCurrentItem(tag, true)
        }
    }

    override fun onBackAction() {
        if (fragments.isNotEmpty() && fragments[currentIndex].isSaving()) {
            fragments[currentIndex].showToSaveDialog {
                super.onBackAction()
            }
        } else {
            super.onBackAction()
        }
    }
}