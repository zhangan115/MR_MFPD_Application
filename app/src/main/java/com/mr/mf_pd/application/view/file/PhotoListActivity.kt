package com.mr.mf_pd.application.view.file

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mr.mf_pd.application.R
import com.mr.mf_pd.application.common.ConstantStr
import com.mr.mf_pd.application.databinding.PhotoListDataBinding
import com.mr.mf_pd.application.view.base.AbsBaseActivity
import com.sito.tool.library.activity.ShowPhotoListActivity
import com.sito.tool.library.utils.GlideUtils
import kotlinx.android.synthetic.main.activity_photo_list.*
import java.io.File

class PhotoListActivity : AbsBaseActivity<PhotoListDataBinding>() {

    var file: File? = null

    override fun initView(savedInstanceState: Bundle?) {

    }

    override fun getToolBarTitle(): String {
        return "查看照片"
    }

    override fun initData(savedInstanceState: Bundle?) {
        val fileDir = intent.getStringExtra(ConstantStr.KEY_BUNDLE_STR)
        if (fileDir != null) {
            file = File(fileDir)
        }
        val files = file?.listFiles()?.filter {
            it.name.endsWith(".png") || it.name.endsWith(".jpg") || it.name.endsWith(".jpeg")
        }
        if (files != null) {
            recycleView.layoutManager = GridLayoutManager(this, 3)
            recycleView.adapter = PhotoListAdapter(files.toList())
            recycleView.addItemDecoration(SpaceItemDecoration(this, 16))
        }
    }

    override fun getContentView(): Int {
        return R.layout.activity_photo_list
    }

    private inner class PhotoListAdapter(private val dataList: List<File>) :
        RecyclerView.Adapter<PhotoListViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoListViewHolder {
            val view =
                LayoutInflater.from(this@PhotoListActivity)
                    .inflate(R.layout.item_image, parent, false)
            return PhotoListViewHolder(view)
        }

        override fun onBindViewHolder(holder: PhotoListViewHolder, position: Int) {
            val arrayStr: ArrayList<String> = ArrayList()
            dataList.forEach {
                arrayStr.add(it.absolutePath)
            }
            holder.layout.setOnClickListener {
                ShowPhotoListActivity.startActivity(
                    this@PhotoListActivity,
                    arrayStr,
                    position,
                    R.mipmap.img_null
                )
            }
            GlideUtils.ShowImage(
                this@PhotoListActivity,
                dataList[position],
                holder.mFileImage,
                R.mipmap.img_null
            )
        }

        override fun getItemCount(): Int {
            return dataList.size
        }

    }

    internal class PhotoListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val layout: LinearLayout = itemView.findViewById(R.id.layout)
        val mFileImage: ImageView = itemView.findViewById(R.id.image)
    }
}