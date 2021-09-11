package com.mr.mf_pd.application.view.base

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mr.mf_pd.application.R
import com.mr.mf_pd.application.app.MRApplication

import java.io.File

@SuppressLint("Registered")
open class BaseActivity : AppCompatActivity() {

    open var photo: File? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MRApplication.instance.openActivity(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        MRApplication.instance.closeActivity(this)
    }

    override fun startActivity(intent: Intent?) {
        super.startActivity(intent)
    }

    override fun startActivity(intent: Intent?, options: Bundle?) {
        super.startActivity(intent, options)
        overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left)
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right)
    }
}