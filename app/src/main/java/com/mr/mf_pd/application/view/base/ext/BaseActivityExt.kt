package com.mr.mf_pd.application.view.base.ext

import android.Manifest
import android.content.Intent
import android.text.TextUtils
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.afollestad.materialdialogs.LayoutMode
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.bottomsheets.BottomSheet
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.mr.mf_pd.application.R
import com.mr.mf_pd.application.app.MRApplication
import com.mr.mf_pd.application.view.base.BaseActivity
import com.qw.soul.permission.SoulPermission

import com.qw.soul.permission.bean.Permission
import com.qw.soul.permission.bean.Permissions
import com.qw.soul.permission.callbcak.CheckRequestPermissionsListener
import com.sito.tool.library.activity.ShowPhotoListActivity
import com.sito.tool.library.utils.ActivityUtilsV4
import java.io.File

fun BaseActivity.showChoosePhotoDialog(
    takePhotoRequestCode: Int,
    photoUrl: String? = null,
    filePhoto: File? = null
) {
    val permissions =
        Permissions.build(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    SoulPermission.getInstance().checkAndRequestPermissions(permissions, object :
        CheckRequestPermissionsListener {
        override fun onPermissionDenied(refusedPermissions: Array<out Permission>) {
            MaterialDialog(this@showChoosePhotoDialog).show {
                title(null, "提示")
                message(null, "请打开相关权限，否则APP无法提供相关功能")
                positiveButton(R.string.sure)
                negativeButton(R.string.cancel)
                positiveButton {
                    it.dismiss()
                    SoulPermission.getInstance().goApplicationSettings()
                }
            }
        }

        override fun onAllPermissionOk(allPermissions: Array<out Permission>?) {
            MaterialDialog(this@showChoosePhotoDialog, BottomSheet(LayoutMode.WRAP_CONTENT)).show {
                customView(
                    R.layout.dialog_choose_photo,
                    noVerticalPadding = true,
                    horizontalPadding = true,
                    dialogWrapContent = true
                )
                if (!TextUtils.isEmpty(photoUrl)) {
                    findViewById<LinearLayout>(R.id.checkPhotoLayout).visibility = View.VISIBLE
                    findViewById<LinearLayout>(R.id.checkPhotoLayout).setOnClickListener {
                        if (TextUtils.isEmpty(photoUrl)) return@setOnClickListener
                        when {
                            photoUrl!!.startsWith("http") -> {
                                ShowPhotoListActivity.startActivity(
                                    this@showChoosePhotoDialog,
                                    photoUrl,
                                    0,
                                    R.mipmap.emptyimg
                                )
                            }
                            photoUrl.startsWith(MRApplication.instance.imageCacheFile()) -> {
                                ShowPhotoListActivity.startActivity(
                                    this@showChoosePhotoDialog,
                                    photoUrl,
                                    0,
                                    R.mipmap.emptyimg
                                )
                            }
                            photoUrl.startsWith("/") -> {
                                val host = MRApplication.appHost().replace("api/", "")
                                val newUrl = photoUrl.replaceFirst("/", "")
                                ShowPhotoListActivity.startActivity(
                                    this@showChoosePhotoDialog,
                                    host + newUrl,
                                    0,
                                    R.mipmap.emptyimg
                                )
                            }
                            else -> {
                                val host = MRApplication.appHost().replace("api/", "")
                                ShowPhotoListActivity.startActivity(
                                    this@showChoosePhotoDialog,
                                    host + photoUrl,
                                    0,
                                    R.mipmap.emptyimg
                                )
                            }
                        }
                        dismiss()
                    }
                }
                findViewById<TextView>(R.id.takePhoto).setOnClickListener {
                    if (photo == null) {
                        photo = File(
                            MRApplication.instance.imageCacheFile(),
                            System.currentTimeMillis().toString() + ".jpg"
                        )
                    }
                    photo!!.let {
                        if (!it.exists()) {
                            it.createNewFile()
                        }
                    }
                    ActivityUtilsV4.startCameraToPhotoV1(
                        this@showChoosePhotoDialog,
                        photo,
                        takePhotoRequestCode
                    )
                    dismiss()
                }
                findViewById<TextView>(R.id.choosePhoto).setOnClickListener {
                    val intent = Intent(Intent.ACTION_GET_CONTENT)
                    intent.type = "image/*"
                    startActivityForResult(intent, takePhotoRequestCode)
                    dismiss()
                }
                lifecycleOwner(this@showChoosePhotoDialog)
            }
        }
    })
}