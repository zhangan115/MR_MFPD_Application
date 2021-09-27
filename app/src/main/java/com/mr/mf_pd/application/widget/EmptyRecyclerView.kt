package com.mr.mf_pd.application.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class EmptyRecyclerView : RecyclerView {

    private var mEmptyView: View? = null

    private val observer: AdapterDataObserver = object : AdapterDataObserver() {
        override fun onChanged() {
            super.onChanged()
            checkIfEmpty()
        }
    }

    constructor(context: Context?) : super(context!!)

    constructor(context: Context?, attrs: AttributeSet?) : super(
        context!!, attrs
    )

    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(
        context!!, attrs, defStyle
    )

    fun checkIfEmpty() {
        val adapter = adapter
        if (mEmptyView != null && adapter != null) {
            mEmptyView?.visibility = if (adapter.itemCount > 0) GONE else VISIBLE
        }
    }

    override fun setAdapter(adapter: Adapter<*>?) {
        val oldAdapter = getAdapter()
        oldAdapter?.unregisterAdapterDataObserver(observer)
        super.setAdapter(adapter)
        adapter?.registerAdapterDataObserver(observer)
    }

    fun setEmptyView(mEmptyView: View?) {
        this.mEmptyView = mEmptyView
        checkIfEmpty()
    }
}