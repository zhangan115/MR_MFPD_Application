package com.mr.mf_pd.application.view.base.ext

import android.os.Build
import androidx.appcompat.app.AppCompatActivity

const val ACTION_TAKE_PHOTO = 1000
const val ACTION_CHOOSE_FILE = 2000


fun AppCompatActivity.findColor(id: Int): Int {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        resources.getColor(id, null)
    } else {
        resources.getColor(id)
    }
}

fun AppCompatActivity.findStrById(id: Int): String {
    return resources.getString(id)
}
