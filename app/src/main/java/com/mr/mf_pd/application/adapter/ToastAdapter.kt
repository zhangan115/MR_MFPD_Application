package com.isuo.inspection.application.adapter

import android.text.TextUtils
import android.view.View
import android.widget.Toast
import androidx.databinding.BindingAdapter

object ToastAdapter {

    @JvmStatic
    @BindingAdapter("app:toast")
    fun bindToast(v: View, msg: String?) {
        if (!TextUtils.isEmpty(msg)) {
            Toast.makeText(v.context, msg, Toast.LENGTH_SHORT).show()
        }
    }

}