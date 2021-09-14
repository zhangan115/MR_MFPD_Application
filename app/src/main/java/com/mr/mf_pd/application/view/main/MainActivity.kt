package com.mr.mf_pd.application.view.main

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.mr.mf_pd.application.BR
import com.mr.mf_pd.application.R
import com.mr.mf_pd.application.adapter.GenericQuickAdapter
import com.mr.mf_pd.application.common.ConstantStr
import com.mr.mf_pd.application.databinding.MainDataBinding
import com.mr.mf_pd.application.model.DeviceBean
import com.mr.mf_pd.application.utils.getViewModelFactory
import com.mr.mf_pd.application.view.base.AbsBaseActivity
import com.mr.mf_pd.application.view.main.check.DeviceCheckActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AbsBaseActivity<MainDataBinding>() {

    private val viewModel by viewModels<MainViewModel> { getViewModelFactory() }
    var dataList = ArrayList<DeviceBean>()

    override fun initView(savedInstanceState: Bundle?) {
        val adapter = GenericQuickAdapter(
            R.layout.item_device_list, this.dataList, BR.deviceBean
        )
        recycleView.layoutManager = LinearLayoutManager(this)
        recycleView.adapter = adapter
        adapter.addChildClickViewIds(R.id.layout_item_root)
        adapter.setOnItemChildClickListener { _, _, position ->
            val intent = Intent(this, DeviceCheckActivity::class.java)
            intent.putExtra(ConstantStr.KEY_BUNDLE_OBJECT,dataList[position])
            startActivity(intent)
        }
        checkDataLayout.setOnClickListener {

        }
        checkTaskLayout.setOnClickListener {

        }
        settingLayout.setOnClickListener {

        }
        refreshLayout.setOnRefreshListener {
            refreshLayout.finishRefresh(2000)
        }
        refreshLayout.setEnableLoadMore(false)
    }

    override fun initData(savedInstanceState: Bundle?) {
        dataBinding.vm = viewModel
        viewModel.start()
        dataList.add(DeviceBean("设备1", "xxx-xxx-xxxx", -84, 80, 1, "yyyy-yyyy", 1))
        dataList.add(DeviceBean("设备2", "xxx-xxx-xxxx", -84, 40, 0, "yyyy-yyyy", 1))
        dataList.add(DeviceBean("设备3", "xxx-xxx-xxxx", -84, 50, 0, "yyyy-yyyy", 1))
        recycleView.adapter?.notifyDataSetChanged()
    }

    override fun getContentView(): Int {
        return R.layout.activity_main
    }

}