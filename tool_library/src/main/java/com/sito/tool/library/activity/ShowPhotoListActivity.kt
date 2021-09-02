package com.sito.tool.library.activity

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import android.widget.ImageView.ScaleType
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.sito.tool.library.R
import com.sito.tool.library.widget.ExtendedViewPager
import com.sito.tool.library.widget.TouchImageView
import kotlin.collections.ArrayList

class ShowPhotoListActivity : AppCompatActivity() {

    private var mUrls: ArrayList<String>? = null
    private var mImageViews: Array<ImageView?>? = null
    private var bmp: Bitmap? = null

    private var mIndex = 0
    private var mCount = 0

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.photos)
        val bData = intent.extras!!
        mUrls = bData.getStringArrayList(KEY_URL)
        mIndex = bData.getInt(KEY_ID, 0)
        if (mUrls == null) {
            finish()
            return
        }
        mCount = mUrls!!.size
        for (mUrl in mUrls!!) {
            if (TextUtils.isEmpty(mUrl)) {
                mCount -= 1
            }
        }
        transparentStatusBar()
        setDarkStatusIcon()
        initView()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (bmp != null) {
            bmp!!.recycle()
            bmp = null
        }
        System.gc()
    }

    private fun initView() {
        mImageViews = arrayOfNulls(mUrls!!.size)
        val group = findViewById<ViewGroup>(R.id.viewGroup)
        if (mCount > 1) {
            for (i in mUrls!!.indices) {
                val imageView = ImageView(this)
                imageView.layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                if (i == mUrls!!.size - 1) {
                    imageView.setPadding(0, 0, 0, 0)
                } else {
                    imageView.setPadding(0, 0, 10, 0)
                }
                imageView.transitionName = mTransitionName
                mImageViews!![i] = imageView
                if (i == 0) {
                    mImageViews!![i]!!.setImageResource(R.drawable.page003)
                } else {
                    mImageViews!![i]!!.setImageResource(R.drawable.page004)
                }
                group.addView(mImageViews!![i])
            }
        } else {
            group.visibility = View.GONE
        }
        val mViewPager: ExtendedViewPager = findViewById(R.id.view_pager)
        mViewPager.adapter = TouchImageAdapter()
        mViewPager.addOnPageChangeListener(MyOnPageChangeListener())
        mViewPager.currentItem = mIndex
    }

    private val mOnClickListener =
        View.OnClickListener { finish() }

    private inner class TouchImageAdapter : PagerAdapter() {

        override fun getCount(): Int {
            return mCount
        }

        override fun instantiateItem(
            container: ViewGroup,
            position: Int
        ): View {
            val img = TouchImageView(container.context)
            img.scaleType = ScaleType.FIT_XY
            img.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            img.adjustViewBounds = true
            img.setOnClickListener(mOnClickListener)
            val options: RequestOptions = RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .error(getDrawable(drawableRes))
                .placeholder(getDrawable(drawableRes))
            Glide.with(this@ShowPhotoListActivity).load(mUrls!![position])
                .apply(options)
                .thumbnail(0.1f)
                .into(img)
            container.addView(img)
            return img
        }

        override fun destroyItem(
            container: ViewGroup,
            position: Int,
            `object`: Any
        ) {
            container.removeView(`object` as View)
        }

        override fun isViewFromObject(
            view: View,
            `object`: Any
        ): Boolean {
            return view === `object`
        }
    }

    internal inner class MyOnPageChangeListener : ViewPager.OnPageChangeListener {

        override fun onPageScrollStateChanged(arg0: Int) {}
        override fun onPageScrolled(arg0: Int, arg1: Float, arg2: Int) {}
        override fun onPageSelected(index: Int) {
            var index1 = index
            index1 %= mUrls!!.size
            val images = mImageViews
            for (i in mUrls!!.indices) {
                images?.get(index1)!!.setImageResource(R.drawable.page003)
                if (index1 != i) {
                    images[i]!!.setImageResource(R.drawable.page004)
                }
            }
        }
    }

    companion object {

        private const val KEY_URL = "key_url"
        private const val KEY_ID = "key_id"
        private var drawableRes: Int = 0
        private const val mTransitionName = "transition_image_view"

        fun startActivity(
            context: Context,
            urls: ArrayList<String>,
            index: Int,
            drawableRes: Int
        ) {
            val bundle = Bundle()
            bundle.putStringArrayList(KEY_URL, urls)
            bundle.putInt(KEY_ID, index)
            val intent = Intent(context, ShowPhotoListActivity::class.java)
            intent.putExtras(bundle)
            context.startActivity(intent)
            this.drawableRes = drawableRes
        }

        fun startActivity(
            context: Context,
            url: String,
            index: Int,
            drawableRes: Int
        ) {
            val bundle = Bundle()
            bundle.putStringArrayList(KEY_URL, arrayListOf(url))
            bundle.putInt(KEY_ID, index)
            val intent = Intent(context, ShowPhotoListActivity::class.java)
            intent.putExtras(bundle)
            context.startActivity(intent)
            this.drawableRes = drawableRes
        }
    }

    /**
     * 状态栏完全透明
     */
    private fun transparentStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //兼容5.0及以上支持全透明
            this.window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            this.window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            this.window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            this.window.statusBarColor = Color.TRANSPARENT
        }
    }

    /**
     * 修改状态栏icon 颜色
     */
    private fun setDarkStatusIcon() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val decorView = window.decorView
            var vis = decorView.systemUiVisibility
            vis = vis and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
            decorView.systemUiVisibility = vis
        }
    }

}