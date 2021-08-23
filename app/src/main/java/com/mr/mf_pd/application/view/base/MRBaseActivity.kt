package com.mr.mf_pd.application.view.base

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.databinding.ViewDataBinding
import com.anson.support.base.BaseActivity

abstract class MRBaseActivity<T : ViewDataBinding>:BaseActivity() {

    lateinit var dataBinding: T

    override fun getContentView(): Int {
        TODO("Not yet implemented")
    }

    override fun getToolBar(): Toolbar? {
        TODO("Not yet implemented")
    }

    override fun getToolBarTitleView(): TextView? {
        TODO("Not yet implemented")
    }

    override fun initData(savedInstanceState: Bundle?) {
        TODO("Not yet implemented")
    }

    override fun initView(savedInstanceState: Bundle?) {
        TODO("Not yet implemented")
    }


}