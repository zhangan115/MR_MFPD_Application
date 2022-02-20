package com.mr.mf_pd.application.view.base

import android.os.Bundle
import android.widget.TextView
import androidx.databinding.ViewDataBinding
import com.afollestad.materialdialogs.MaterialDialog
import com.mr.mf_pd.application.R
import com.mr.mf_pd.application.view.callback.CheckActionListener

/**
 * 检测类型页面的基类
 * @author zhangan
 * @since 2021-11-28
 */
abstract class BaseCheckFragment<T : ViewDataBinding> : BaseFragment<T>() {

    var checkActionListener: CheckActionListener? = null

    /**
     * 显示提示保存的Dialog
     */
    open fun showToSaveDialog(onCancel: () -> Unit) {
        MaterialDialog(requireContext()).show {
            setContentView(R.layout.dialog_save_data)
            findViewById<TextView>(R.id.cancelTv).setOnClickListener {
                onCancel()
                cancelSaveData()
                this.dismiss()
            }
            findViewById<TextView>(R.id.sureTv).setOnClickListener {
                toSaveData2File()
                this.dismiss()
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (activity is BaseCheckActivity<*> && activity is CheckActionListener) {
            checkActionListener = activity as CheckActionListener
        }
    }

    /**
     * 保存数据
     */
    abstract fun toSaveData2File()

    /**
     * 数据是否正在保存
     */
    abstract fun isSaving(): Boolean

    /***
     * 取消数据保存
     */
    abstract fun cancelSaveData()


    fun changeBandDetectionModel() {
        checkActionListener?.changeBandDetectionModel()
    }

    fun addLimitValue() {
        checkActionListener?.addLimitValue()
    }

    fun downLimitValue() {
        checkActionListener?.downLimitValue()
    }
}