package com.mr.mf_pd.application.view.file.listener

import android.os.SystemClock
import android.view.View

internal interface OnItemPhotoClickListener {
    fun onItemClick(view: View?, position: Int)
    abstract class ThrottleClickListener : OnItemPhotoClickListener {
        private var mLastClickTime: Long = 0
        abstract fun onItemClickThrottled(view: View?, position: Int)
        override fun onItemClick(view: View?, position: Int) {
            val currentClickTime = SystemClock.uptimeMillis()
            val elapsedTime = currentClickTime - mLastClickTime
            mLastClickTime = currentClickTime
            if (elapsedTime <= MIN_CLICK_INTERVAL) {
                return
            }
            onItemClickThrottled(view, position)
        }

        companion object {
            private const val MIN_CLICK_INTERVAL: Long = 600
        }
    }
}