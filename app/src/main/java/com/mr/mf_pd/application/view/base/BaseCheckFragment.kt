package com.mr.mf_pd.application.view.base

import androidx.databinding.ViewDataBinding
import com.afollestad.materialdialogs.MaterialDialog
import com.mr.mf_pd.application.R
import com.mr.mf_pd.application.view.callback.CheckStateChange

abstract class BaseCheckFragment<T : ViewDataBinding> : BaseFragment<T>(), CheckStateChange {

    /**
     * 显示提示保存的Dialog
     */
    private fun showToSaveDialog() {
        MaterialDialog(requireContext()).show {
            title(text = "提示")
            message(text = "是否保存当前的数据?")
            cancelable(false)
            positiveButton(R.string.ok)
            positiveButton {
                toSaveData2File()
                it.dismiss()
            }
            negativeButton(R.string.cancel)
            negativeButton {
                it.dismiss()
            }
        }
    }

    abstract fun toSaveData2File()

}