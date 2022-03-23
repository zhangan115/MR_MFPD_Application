package com.mr.mf_pd.application.view.base

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.MutableLiveData
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.google.common.eventbus.EventBus
import com.google.common.eventbus.Subscribe
import com.mr.mf_pd.application.R
import com.mr.mf_pd.application.common.ConstantStr
import com.mr.mf_pd.application.common.Constants
import com.mr.mf_pd.application.model.SettingBean
import com.mr.mf_pd.application.repository.DefaultDataRepository
import com.mr.mf_pd.application.repository.DefaultFilesRepository
import com.mr.mf_pd.application.utils.ByteUtil
import com.mr.mf_pd.application.view.callback.CheckActivityListener
import com.mr.mf_pd.application.view.callback.FragmentDataListener
import com.mr.mf_pd.application.view.file.FilePickerActivity
import java.text.DecimalFormat

/**
 * 检测类型页面的基类
 * @author zhangan
 * @since 2021-11-28
 */
abstract class BaseCheckFragment<T : ViewDataBinding> : BaseFragment<T>(), FragmentDataListener {
    open var TAG = "BaseCheckFragment"
    var checkActionListener: CheckActivityListener? = null
    var location: String? = null
    private val requestChooseDirCode = 200
    var isOpenFromFile = false
    private var settingBeanBr: SettingChangeBr? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val isFile = arguments?.getBoolean(ConstantStr.KEY_BUNDLE_BOOLEAN)
        setIsFileValue(isFile)
    }

    override fun onResume() {
        super.onResume()
        Log.d("zhangan", "$TAG onResume")
    }

    override fun onPause() {
        super.onPause()
        Log.d("zhangan", "$TAG onPause")
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
        settingBeanBr = SettingChangeBr()
        val filler = IntentFilter(Constants.UPDATE_SETTING)
        LocalBroadcastManager.getInstance(requireActivity())
            .registerReceiver(settingBeanBr!!, filler)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (settingBeanBr != null) {
            LocalBroadcastManager.getInstance(requireActivity()).unregisterReceiver(settingBeanBr!!)
        }
    }

    fun getUnitValue(settingBean: SettingBean): ArrayList<String> {
        val unitList = ArrayList<String>()
        if (settingBean.fzUnit != null) {
            unitList.add(settingBean.fzUnit!!)
        }
        return unitList
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
            val df1 = DecimalFormat("0.0")
            val value = maxValue - minValue
            val step = value / 4.0f
            for (i in 0..4) {
                yList.add(df1.format(minValue + step * i))
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

    abstract fun updateSettingBean(settingBean: SettingBean)

    private inner class SettingChangeBr : BroadcastReceiver() {

        override fun onReceive(context: Context?, intent: Intent?) {
            val settingBean = intent?.getParcelableExtra<SettingBean>(ConstantStr.KEY_BUNDLE_OBJECT)
            if (settingBean != null) {
                updateSettingBean(settingBean)
            }
        }

    }
}