package com.mr.mf_pd.application.view.base

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.databinding.ViewDataBinding
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.mr.mf_pd.application.R
import com.mr.mf_pd.application.common.ConstantStr
import com.mr.mf_pd.application.view.callback.CheckActionListener
import com.mr.mf_pd.application.view.file.FilePickerActivity

/**
 * 检测类型页面的基类
 * @author zhangan
 * @since 2021-11-28
 */
abstract class BaseCheckFragment<T : ViewDataBinding> : BaseFragment<T>() {

    var checkActionListener: CheckActionListener? = null
    var location: String? = null
    private val requestChooseDirCode = 200

    open fun showSaveFileDialog() {
        activity?.let {
            MaterialDialog(it).show {
                it.setTheme(R.style.AppTheme_MaterialDialog)
                title(text = "提示")
                message(text = "是否保存当前数据？")
                positiveButton(res = R.string.ok, click = { dialog ->
                    dialog.dismiss()
                    if (getLocationValue() == null) {
                        //选择保存位置
                        val intent = Intent(activity, FilePickerActivity::class.java)
                        intent.putExtra(ConstantStr.KEY_BUNDLE_BOOLEAN, true)
                        startActivityForResult(intent, requestChooseDirCode)
                    } else {
                        createACheckFile()
                    }
                })
                negativeButton(res = R.string.cancel, click = { dialog ->
                    dialog.dismiss()
                })
                lifecycleOwner(this@BaseCheckFragment)
            }
        }
    }

    fun getLocationValue(): String? {
        return location
    }

    fun setLocationValue(location: String?) {
        this.location = location
    }

    open fun setCheckFile(str:String) {

    }

    open fun createACheckFile() {

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == requestChooseDirCode && resultCode == Activity.RESULT_OK) {
            val fileDir = data?.getStringExtra(ConstantStr.KEY_BUNDLE_STR)
            fileDir?.let {
                setLocationValue(fileDir)
                setCheckFile(it)
            }
        }
    }

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