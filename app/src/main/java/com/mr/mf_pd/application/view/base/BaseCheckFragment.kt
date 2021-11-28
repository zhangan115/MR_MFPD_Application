package com.mr.mf_pd.application.view.base

import android.widget.TextView
import androidx.databinding.ViewDataBinding
import com.afollestad.materialdialogs.MaterialDialog
import com.mr.mf_pd.application.R

/**
 * 检测类型页面的基类
 * @author zhangan
 * @since 2021-11-28
 */
abstract class BaseCheckFragment<T : ViewDataBinding> : BaseFragment<T>() {

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

}