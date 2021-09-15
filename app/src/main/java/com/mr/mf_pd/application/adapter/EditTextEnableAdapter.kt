package com.mr.mf_pd.application.adapter

import android.widget.EditText
import androidx.databinding.BindingAdapter
import com.mr.mf_pd.application.R

object EditTextEnableAdapter {

    @JvmStatic
    @BindingAdapter(
        "app:edit_enable",
    )
    fun bindEditTextEnable(
        et: EditText,
        isEnable: Boolean = true
    ) {
        if (isEnable) {
            et.setTextColor(et.context.resources.getColor(R.color.text_content_secondary_color))
        } else {
            et.setTextColor(et.context.resources.getColor(R.color.text_second_title))
        }
        et.isEnabled = isEnable
    }
}