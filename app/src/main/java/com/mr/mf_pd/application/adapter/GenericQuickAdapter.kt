package com.mr.mf_pd.application.adapter

import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder

import java.util.*

class GenericQuickAdapter<X>(layoutResId: Int, data: MutableList<X>?, private val name: Int) :
    BaseQuickAdapter<X, GenericQuickAdapter.GenericViewHolder>(layoutResId, data) {

    override fun convert(helper: GenericViewHolder, item: X) {
        Objects.requireNonNull(helper.getBinding<ViewDataBinding>()).setVariable(name, item)
    }

    class GenericViewHolder(view: View) : BaseViewHolder(view) {
        private val mBinding: ViewDataBinding? = DataBindingUtil.bind(view)
        override fun <B : ViewDataBinding> getBinding(): B {
            return mBinding as B
        }
    }
}