/*
 * Copyright (C) 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.mr.mf_pd.application.view.base.ext

/**
 * Extension functions for Fragment.
 */

import android.Manifest
import androidx.fragment.app.Fragment
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.mr.mf_pd.application.factory.ViewModelFactory
import java.io.File
import android.content.Intent
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.afollestad.materialdialogs.LayoutMode
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.bottomsheets.BottomSheet
import com.mr.mf_pd.application.R
import com.mr.mf_pd.application.app.MRApplication
import com.mr.mf_pd.application.view.base.BaseFragment
import com.qw.soul.permission.SoulPermission
import com.qw.soul.permission.bean.Permission
import com.qw.soul.permission.bean.Permissions
import com.qw.soul.permission.callbcak.CheckRequestPermissionsListener
import com.sito.tool.library.utils.ActivityUtilsV4



fun Fragment.getViewModelFactory(): ViewModelFactory {
    val userRepository =
        (requireContext().applicationContext as MRApplication).userRepository
    val dataRepository =
        (requireContext().applicationContext as MRApplication).dataRepository
    val settingRepository =
        (requireContext().applicationContext as MRApplication).settingRepository
    val filesRepository =
        (requireContext().applicationContext as MRApplication).filesRepository
    return ViewModelFactory(dataRepository,filesRepository, settingRepository, userRepository, this)
}

fun BaseFragment<*>.takePhotoAndChoosePhoto(
    takePhotoRequestCode: Int,
    showPhotoLayout: Boolean = true,
    checkPhotoListener: () -> Unit
) {
    val permissions =
        Permissions.build(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    SoulPermission.getInstance().checkAndRequestPermissions(permissions, object :
        CheckRequestPermissionsListener {
        override fun onPermissionDenied(refusedPermissions: Array<out Permission>) {
            activity?.let {
                MaterialDialog(it).show {
                    title(text = "提示")
                    message(text = "请打开相关权限，否则APP无法提供相关功能")
                    negativeButton(res = R.string.cancel) { dialog ->
                        dialog.dismiss()
                    }
                    positiveButton(res = R.string.sure) { dialog ->
                        dialog.dismiss()
                        SoulPermission.getInstance().goApplicationSettings()
                    }
                }
            }
        }

        override fun onAllPermissionOk(allPermissions: Array<out Permission>?) {
            MaterialDialog(
                this@takePhotoAndChoosePhoto.activity!!,
                BottomSheet(LayoutMode.WRAP_CONTENT)
            ).show {
                customView(
                    R.layout.dialog_choose_photo,
                    noVerticalPadding = true,
                    horizontalPadding = true,
                    dialogWrapContent = true
                )
                if (showPhotoLayout) {
                    findViewById<LinearLayout>(R.id.checkPhotoLayout).visibility = View.VISIBLE
                    findViewById<TextView>(R.id.checkPhoto).setOnClickListener {
                        checkPhotoListener()
                    }
                } else {
                    findViewById<LinearLayout>(R.id.checkPhotoLayout).visibility = View.GONE
                }
                findViewById<TextView>(R.id.takePhoto).setOnClickListener {
                    photo = File(
                        MRApplication.instance.imageCacheFile(),
                        System.currentTimeMillis().toString() + ".jpg"
                    )
                    if (!photo!!.exists()) {
                        photo!!.createNewFile()
                    }
                    ActivityUtilsV4.startCameraToPhotoV1(
                        this@takePhotoAndChoosePhoto.activity,
                        photo,
                        takePhotoRequestCode
                    )
                    dismiss()
                }
                findViewById<TextView>(R.id.choosePhoto).setOnClickListener {
                    photo = null
                    val intent = Intent(Intent.ACTION_GET_CONTENT)
                    intent.type = "image/*"
                    startActivityForResult(intent, takePhotoRequestCode)
                    dismiss()
                }
                lifecycleOwner(this@takePhotoAndChoosePhoto)
            }
        }
    })
}

fun BaseFragment<*>.takePhoto(
    takePhotoRequestCode: Int,
) {
    val permissions =
        Permissions.build(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    SoulPermission.getInstance().checkAndRequestPermissions(permissions, object :
        CheckRequestPermissionsListener {
        override fun onPermissionDenied(refusedPermissions: Array<out Permission>) {
            activity?.let {
                MaterialDialog(it).show {
                    title(text = "提示")
                    message(text = "请打开相关权限，否则APP无法提供相关功能")
                    negativeButton(res = R.string.cancel) { dialog ->
                        dialog.dismiss()
                    }
                    positiveButton(res = R.string.sure) { dialog ->
                        dialog.dismiss()
                        SoulPermission.getInstance().goApplicationSettings()
                    }
                }
            }
        }

        override fun onAllPermissionOk(allPermissions: Array<out Permission>?) {
            photo = File(
                MRApplication.instance.imageCacheFile(),
                System.currentTimeMillis().toString() + ".jpg"
            )
            photo?.let {
                if (!it.exists()) {
                    it.createNewFile()
                }
                ActivityUtilsV4.startCameraToPhotoV1(
                    this@takePhoto,
                    photo,
                    takePhotoRequestCode
                )
            }

        }
    })
}

fun BaseFragment<*>.choosePhoto(
    takePhotoRequestCode: Int,
) {
    val permissions =
        Permissions.build(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    SoulPermission.getInstance().checkAndRequestPermissions(permissions, object :
        CheckRequestPermissionsListener {
        override fun onPermissionDenied(refusedPermissions: Array<out Permission>) {
            activity?.let {
                MaterialDialog(it).show {
                    title(text = "提示")
                    message(text = "请打开相关权限，否则APP无法提供相关功能")
                    negativeButton(res = R.string.cancel) { dialog ->
                        dialog.dismiss()
                    }
                    positiveButton(res = R.string.sure) { dialog ->
                        dialog.dismiss()
                        SoulPermission.getInstance().goApplicationSettings()
                    }
                }
            }
        }

        override fun onAllPermissionOk(allPermissions: Array<out Permission>?) {
            photo = null
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            startActivityForResult(intent, takePhotoRequestCode)
        }
    })
}