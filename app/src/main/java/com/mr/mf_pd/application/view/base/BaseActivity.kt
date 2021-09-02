package com.mr.mf_pd.application.view.base

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
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
}