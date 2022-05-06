package com.mr.mf_pd.application.view.base

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import androidx.databinding.ViewDataBinding
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.afollestad.materialdialogs.list.listItems
import com.mr.mf_pd.application.R
import com.mr.mf_pd.application.common.CheckType
import com.mr.mf_pd.application.common.ConstantStr
import kotlinx.android.synthetic.main.activity_uhf_setting.*

abstract class BaseSettingActivity<T : ViewDataBinding> : AbsBaseActivity<T>(),
    View.OnClickListener {

    open lateinit var checkType: CheckType

    override fun initView(savedInstanceState: Bundle?) {
        getPhaseModelLayout()?.setOnClickListener {
            //相位同步
            MaterialDialog(this)
                .show {
                    listItems(R.array.choose_phase_model) { _, index, text ->
                        onPhaseModelChange(text.toString(), index)
                    }
                    lifecycleOwner(this@BaseSettingActivity)
                }
        }
        getBandDetectionLayout()?.setOnClickListener {
            //检测频带
            MaterialDialog(this)
                .show {
                    listItems(R.array.choose_band_detection) { _, index, text ->
                        onBandDetectionChange(text.toString(), index)
                    }
                    lifecycleOwner(this@BaseSettingActivity)
                }
        }
    }

    override fun initData(savedInstanceState: Bundle?) {
        checkType = intent.getSerializableExtra(ConstantStr.KEY_BUNDLE_OBJECT) as CheckType
    }

    override fun getToolBarTitle(): String {
        return findString(checkType.description) + "设置"
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_setting, menu)
        return false
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu_setting) {

        }
        return true
    }

    override fun onClick(v: View?) {

    }

    abstract fun getPhaseModelLayout(): LinearLayout?

    abstract fun getBandDetectionLayout(): LinearLayout?

    abstract fun onPhaseModelChange(text: String, index: Int)

    abstract fun onBandDetectionChange(text: String, index: Int)

}