package com.mr.mf_pd.application.adapter

import android.view.Gravity
import android.widget.Button
import androidx.databinding.BindingAdapter
import com.mr.mf_pd.application.R

object CommonButtonAdapter {

    @JvmStatic
    @BindingAdapter(
        "app:button_common_enable"
    )
    fun bindCommonButton(
        button: Button,
        isCommonEnable: Boolean = false
    ) {
        button.textSize = 16f
        button.isEnabled = isCommonEnable
        button.gravity = Gravity.CENTER
        if (isCommonEnable) {
            button.background =
                button.context.resources.getDrawable(R.drawable.button_enable_common, null)
            button.setTextColor(button.context.resources.getColor(R.color.button_enable_text))
        } else {
            button.background =
                button.context.resources.getDrawable(R.drawable.button_disabled_gray, null)
            button.setTextColor(button.context.resources.getColor(R.color.button_disabled_text))
        }
    }
}