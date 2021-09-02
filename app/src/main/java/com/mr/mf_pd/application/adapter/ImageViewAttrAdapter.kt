package com.mr.mf_pd.application.adapter

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.text.TextUtils
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.mr.mf_pd.application.app.MRApplication
import com.sito.tool.library.utils.GlideUtils

object ImageViewAttrAdapter {

    @BindingAdapter("android:src")
    fun setSrc(view: ImageView, bitmap: Bitmap?) {
        view.setImageBitmap(bitmap)
    }

    @BindingAdapter("android:src")
    fun setSrc(view: ImageView, resId: Int) {
        view.setImageResource(resId)
    }

    @JvmStatic
    @BindingAdapter("app:imageUrl", "app:placeHolder", "app:error")
    fun loadImage(
        imageView: ImageView,
        url: String?,
        holderDrawable: Drawable?,
        errorDrawable: Drawable?
    ) {
        if (TextUtils.isEmpty(url)) {
            imageView.setImageDrawable(holderDrawable)
            return
        }
        if (url!!.startsWith("http")) {
            Glide.with(imageView.context)
                .load(url)
                .error(errorDrawable)
                .placeholder(holderDrawable)
                .into(imageView)
        } else {
            Glide.with(imageView.context)
                .load(MRApplication.appHost() + url)
                .error(errorDrawable)
                .placeholder(holderDrawable)
                .into(imageView)
        }
    }

    @JvmStatic
    @BindingAdapter("app:cirImageUrl", "app:cirPlaceHolder")
    fun loadCirImage(
        imageView: ImageView,
        url: String?,
        holderDrawable: Drawable?
    ) {
        if (TextUtils.isEmpty(url)) {
            imageView.setImageDrawable(holderDrawable)
            return
        }
        if (url!!.startsWith("http")) {
            GlideUtils.ShowCircleImageWithContext(
                imageView.context,
                url,
                imageView,
                holderDrawable
            )
        } else if (url.startsWith(MRApplication.instance.imageCacheFile())) {
            GlideUtils.ShowCircleImageWithContext(
                imageView.context,
                url,
                imageView,
                holderDrawable
            )
        }else if (url.startsWith("/")) {
            val host = MRApplication.appHost().replace("api/","")
            val  newUrl = url.replaceFirst("/","")
            GlideUtils.ShowCircleImageWithContext(
                imageView.context,
                host + newUrl,
                imageView,
                holderDrawable
            )
        } else {
            val host = MRApplication.appHost().replace("api/","")
            GlideUtils.ShowCircleImageWithContext(
                imageView.context,
                host + url,
                imageView,
                holderDrawable
            )
        }
    }
}