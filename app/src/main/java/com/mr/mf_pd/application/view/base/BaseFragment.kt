package com.mr.mf_pd.application.view.base

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import com.mr.mf_pd.application.common.ConstantInt
import com.mr.mf_pd.application.utils.PhotoCompressUtils
import java.io.File
import java.io.IOException

abstract class BaseFragment<T : ViewDataBinding> : Fragment() {

    open var photo: File? = null

    /**
     * 视图是否已经初初始化
     */
    private var isInit = false
    private var isLoad = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(getContentView(), container, false)
        DataBindingUtil.bind<T>(root).apply {
            this?.let {
                this.lifecycleOwner = this@BaseFragment.viewLifecycleOwner
                setViewModel(this)
            }
        }
        isInit = true
        /**初始化的时候去加载数据**/
        isCanLoadData()
        return root
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        isCanLoadData()
    }

    /**
     * 是否可以加载数据
     * 可以加载数据的条件：
     * 1.视图已经初始化
     * 2.视图对用户可见
     */
    private fun isCanLoadData() {
        if (!isInit) {
            return
        }
        if (userVisibleHint) {
            Log.d("zhangan","lazyLoad")
            lazyLoad()
            isLoad = true
        } else {
            if (isLoad) {
                stopLoad()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        isInit = false
        isLoad = false
    }

    /**
     * 当视图初始化并且对用户可见的时候去真正的加载数据
     */
    protected abstract fun lazyLoad()

    /**
     * 当视图已经对用户不可见并且加载过数据，如果需要在切换到其他页面时停止加载数据，可以调用此方法
     */
    protected open fun stopLoad() {}

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initData()
        initView()
    }

    abstract fun getContentView(): Int

    abstract fun initData()

    abstract fun initView()

    abstract fun setViewModel(dataBinding: T?)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }

    /**
     * 查找颜色
     */
    fun findColor(color: Int): Int {
        return resources.getColor(color, null)
    }

    /**
     * 查找字符串
     */
    fun findString(str: Int): String {
        return resources.getString(str)
    }

    /**
     * 查找图片e
     */
    @SuppressLint("UseCompatLoadingForDrawables")
    fun findDrawable(drawable: Int): Drawable {
        return resources.getDrawable(drawable, null)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode <= ConstantInt.ACTION_TAKE_PHOTO && resultCode == Activity.RESULT_OK) {
            if (data?.data == null && photo != null) {
                if (photo != null) {
                    val photoURI = FileProvider.getUriForFile(
                        this.activity!!,
                        "com.mr.mf_pd.application.fileprovider",
                        photo!!
                    )
                    try {
                        PhotoCompressUtils.getFile(this.activity!!,createTargetDir(), photoURI, {
                            dealFile(requestCode, it)
                        }, {
                            Toast.makeText(this.activity!!, "图片选择失败！", Toast.LENGTH_SHORT).show()
                        })
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
        } else if (requestCode > ConstantInt.ACTION_TAKE_PHOTO && requestCode <= ConstantInt.ACTION_CHOOSE_FILE && resultCode == Activity.RESULT_OK) {
            try {
                PhotoCompressUtils.getFile(this.activity!!,createTargetDir(), data!!.data!!, {
                    dealFile(requestCode, it)
                }, {
                    Toast.makeText(this.activity!!, "图片选择失败！", Toast.LENGTH_SHORT).show()
                })
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    open fun dealFile(requestCode:Int,file:File){

    }

    open fun createTargetDir():String?{
        return null
    }
}
