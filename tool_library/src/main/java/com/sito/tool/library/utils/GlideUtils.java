package com.sito.tool.library.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.widget.ImageView;

import androidx.annotation.DrawableRes;
import androidx.annotation.MainThread;
import androidx.annotation.WorkerThread;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.sito.tool.library.widget.CircleTransform;

import java.io.File;


/**
 * 项目工具类
 * Created by zhangan on 2016-12-06.
 */

public final class GlideUtils {


    /**
     * 使用Glide 显示图片
     *
     * @param activity  activity
     * @param url       地址
     * @param imageView 对象
     */
    public static void ShowImage(Activity activity, String url, ImageView imageView, @DrawableRes int drawableRes) {
        Glide.with(activity).load(url)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .error(drawableRes)
                .placeholder(drawableRes)
                .centerCrop()
                .dontAnimate()
                .into(imageView);
    }


    /**
     * 使用Glide 显示图片
     *
     * @param fragment  fragment
     * @param url       地址
     * @param imageView 对象
     */
    public static void ShowImage(android.app.Fragment fragment, String url, ImageView imageView, @DrawableRes int drawableRes) {
        Glide.with(fragment).load(url)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .error(drawableRes)
                .placeholder(drawableRes)
                .centerCrop()
                .dontAnimate()
                .into(imageView);
    }

    /**
     * 使用Glide 显示图片
     *
     * @param fragment  fragment
     * @param url       地址
     * @param imageView 对象
     */
    public static void ShowImage(Fragment fragment, String url, ImageView imageView, @DrawableRes int drawableRes) {
        Glide.with(fragment).load(url)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .error(drawableRes)
                .placeholder(drawableRes)
                .centerCrop()
                .dontAnimate()
                .into(imageView);
    }

    /**
     * 使用Glide 显示图片
     *
     * @param context   环境
     * @param url       地址
     * @param imageView 对象
     */
    public static void ShowImage(Context context, String url, ImageView imageView, @DrawableRes int drawableRes) {
        Glide.with(context).load(url)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .error(drawableRes)
                .placeholder(drawableRes)
                .centerCrop()
                .dontAnimate()
                .into(imageView);
    }

    /**
     * 使用Glide 显示图片 cookie
     *
     * @param context   环境
     * @param url       地址
     * @param imageView 对象
     */
    public static void ShowImage(Context context, String cookiestr, String url, ImageView imageView, @DrawableRes int drawableRes) {
        GlideUrl cookie = new GlideUrl(url, new LazyHeaders.Builder().addHeader("Cookie", cookiestr).build());
        Glide.with(context).load(cookie)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .error(drawableRes)
                .placeholder(drawableRes)
                .centerCrop()
                .dontAnimate()
                .into(imageView);
    }

    /**
     * 使用Glide 显示图片
     *
     * @param context   环境
     * @param file      文件
     * @param imageView 对象
     */
    public static void ShowImage(Context context, File file, ImageView imageView, @DrawableRes int drawableRes) {
        Glide.with(context).load(file)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .error(drawableRes)
                .placeholder(drawableRes)
                .centerCrop()
                .dontAnimate()
                .into(imageView);
    }

    /**
     * 使用Glide 加载大图片
     *
     * @param activity  环境
     * @param url       地址
     * @param imageView ImageView对象
     */
    public static void ShowBigImage(Activity activity, String url, ImageView imageView, @DrawableRes int drawableRes) {
        Glide.with(activity).load(url)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .error(drawableRes)
                .placeholder(drawableRes)
                .thumbnail(0.1f)
                .into(imageView);
    }

    /**
     * 设置圆形图片
     *
     * @param activity  环境
     * @param url       url
     * @param imageView ImageView 对象
     */
    public static void ShowCircleImage(Activity activity, String url, ImageView imageView, @DrawableRes int drawableRes) {
        if (TextUtils.isEmpty(url)) {
            imageView.setImageDrawable(activity.getDrawable(drawableRes));
            return;
        }
        Glide.with(activity).load(url)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .error(drawableRes)
                .placeholder(drawableRes)
                .transform(new CircleTransform())
                .into(imageView);
    }

    /**
     * 设置圆形图片
     *
     * @param context  环境
     * @param url       url
     * @param imageView ImageView 对象
     */
    public static void ShowCircleImageWithContext(Context context, String url, ImageView imageView, Drawable drawableRes) {
        if (TextUtils.isEmpty(url)) {
            imageView.setImageDrawable(drawableRes);
            return;
        }
        Glide.with(context).load(url)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .error(drawableRes)
                .placeholder(drawableRes)
                .transform(new CircleTransform())
                .into(imageView);
    }

    /**
     * 设置圆形图片 带cookie
     */
    public static void ShowCircleImage(Context context, String cookiestr, String url, ImageView imageView, @DrawableRes int drawableRes) {
        GlideUrl cookie = new GlideUrl(url, new LazyHeaders.Builder().addHeader("Cookie", cookiestr).build());
        Glide.with(context).load(cookie)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .error(drawableRes)
                .placeholder(drawableRes)
                .transform(new CircleTransform())
                .into(imageView);
    }

    /**
     * 设置圆形图片
     *
     * @param context   环境
     * @param url       url
     * @param imageView ImageView 对象
     */
    public static void ShowCircleImage(Context context, String url, ImageView imageView, @DrawableRes int drawableRes) {
        Glide.with(context).load(url)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .error(drawableRes)
                .placeholder(drawableRes)
                .transform(new CircleTransform())
                .into(imageView);
    }

    /**
     * S
     * 清除内存缓存
     *
     * @param context 环境
     */
    @MainThread
    public static void CleanGlideMemory(Context context) {
        Glide.get(context).clearMemory();
    }

    /**
     * 清除文件缓存(需要异步执行)
     *
     * @param context 环境
     */
    @WorkerThread
    public static void cleanGlideDishCache(final Context context) {
        Glide.get(context).clearDiskCache();
    }


}
