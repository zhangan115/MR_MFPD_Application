package com.mr.mf_pd.application.view.base

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.MutableLiveData
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.mr.mf_pd.application.R
import com.mr.mf_pd.application.common.ConstantStr
import com.mr.mf_pd.application.model.SettingBean
import com.mr.mf_pd.application.repository.DefaultDataRepository
import com.mr.mf_pd.application.repository.DefaultFilesRepository
import com.mr.mf_pd.application.utils.ByteUtil
import com.mr.mf_pd.application.view.callback.CheckActivityListener
import com.mr.mf_pd.application.view.callback.FragmentDataListener
import com.mr.mf_pd.application.view.file.FilePickerActivity

/**
 * 检测类型页面的基类
 * @author zhangan
 * @since 2021-11-28
 */
abstract class BaseCheckFragment<T : ViewDataBinding> : BaseFragment<T>(), FragmentDataListener {

    var checkActionListener: CheckActivityListener? = null
    var location: String? = null
    private val requestChooseDirCode = 200
    var isOpenFromFile = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val isFile = arguments?.getBoolean(ConstantStr.KEY_BUNDLE_BOOLEAN)
        setIsFileValue(isFile)
    }

    abstract fun setIsFileValue(isFile: Boolean?)

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
                        createCheckFile()
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

    open fun setCheckFile(str: String) {

    }

    open fun createCheckFile() {

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == requestChooseDirCode && resultCode == Activity.RESULT_OK) {
            val fileDir = data?.getStringExtra(ConstantStr.KEY_BUNDLE_STR)
            fileDir?.let {
                location = fileDir
                setLocationValue(fileDir)
                setCheckFile(it)
            }
        }
    }

    /**
     * 显示提示保存的Dialog
     */
    open fun showToSaveDialog(onCancel: () -> Unit) {
        activity?.let {
            MaterialDialog(it).show {
                it.setTheme(R.style.AppTheme_MaterialDialog)
                title(text = "提示")
                message(text = "是否保存当前数据？")
                positiveButton(res = R.string.ok, click = { dialog ->
                    cancelSaveData()
                    dialog.dismiss()
                    if (getLocationValue() == null) {
                        //选择保存位置
                        val intent = Intent(activity, FilePickerActivity::class.java)
                        intent.putExtra(ConstantStr.KEY_BUNDLE_BOOLEAN, true)
                        startActivityForResult(intent, requestChooseDirCode)
                    } else {
                        createCheckFile()
                    }
                })
                negativeButton(res = R.string.cancel, click = { dialog ->
                    cancelSaveData()
                    onCancel()
                    dialog.dismiss()
                })
                lifecycleOwner(this@BaseCheckFragment)
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (activity is BaseCheckActivity<*> && activity is CheckActivityListener) {
            checkActionListener = activity as CheckActivityListener
        }
    }

    fun getYAxisValue(
        isFile: Boolean,
        settingBean: SettingBean,
        gainMinValue: MutableLiveData<Float?>,
    ): ArrayList<String> {
        val yList = ArrayList<String>()
        val minValue: Float?
        val maxValue: Float?
        if (settingBean.gdCd != 1) {
            if (isFile) {
                minValue = DefaultFilesRepository.realDataMinValue.value?.toFloat()
                maxValue = DefaultFilesRepository.realDataMaxValue.value?.toFloat()
            } else {
                minValue = DefaultDataRepository.realDataMinValue.value?.toFloat()
                maxValue = DefaultDataRepository.realDataMaxValue.value?.toFloat()
            }
        } else {
            minValue = settingBean.minValue.toFloat()
            maxValue = settingBean.maxValue.toFloat()
        }
        if (minValue != null && maxValue != null) {
            val value =
                maxValue - minValue
            val step = value / 4.00f
            for (i in 0..4) {
                val str = minValue + step * i
                yList.add(str.toString())
            }
            gainMinValue.postValue(minValue)
        }
        yList.reverse()
        return yList
    }


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

    fun splitBytesToValue(bytes: ByteArray): ArrayList<Float> {
        val valueList = ArrayList<Float>()
        if (bytes.size > 2) {
            val length = bytes[2].toInt()
            val source = ByteArray(length * 4)
            System.arraycopy(bytes, 3, source, 0, bytes.size - 5)
            for (i in 0 until (source.size / 4)) {
                val value = ByteArray(4)
                System.arraycopy(source, 4 * i, value, 0, 4)
                val f = ByteUtil.getFloat(value)
                valueList.add(f)
            }
        }
        return valueList
    }
}