package com.mr.mf_pd.application.view.file.adapter

import android.annotation.SuppressLint
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.mr.mf_pd.application.R
import com.mr.mf_pd.application.app.MRApplication
import com.mr.mf_pd.application.utils.FileTypeUtils
import com.mr.mf_pd.application.view.file.listener.OnItemClickListener
import com.mr.mf_pd.application.view.file.adapter.DirectoryAdapter.DirectoryViewHolder
import com.mr.mf_pd.application.view.file.listener.OnItemLabelClickListener
import com.mr.mf_pd.application.view.file.listener.OnItemPhotoClickListener
import com.mr.mf_pd.application.view.file.model.CheckDataFileModel

internal class DirectoryAdapter(private val dataList: List<CheckDataFileModel>) :
    RecyclerView.Adapter<DirectoryViewHolder>() {

    private var mOnItemClickListener: OnItemClickListener? = null
    private var mOnPhotoClickListener: OnItemPhotoClickListener? = null
    private var mOnLabelClickListener: OnItemLabelClickListener? = null

    fun setOnItemClickListener(listener: OnItemClickListener?) {
        mOnItemClickListener = listener
    }

    fun setOnPhotoClickListener(listener: OnItemPhotoClickListener?) {
        mOnPhotoClickListener = listener
    }

    fun setOnLabelClickListener(listener: OnItemLabelClickListener?) {
        mOnLabelClickListener = listener
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DirectoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_file, parent, false)
        return DirectoryViewHolder(
            view,
            mOnPhotoClickListener,
            mOnLabelClickListener
        )
    }

    @SuppressLint("SetTextI18n", "UseCompatLoadingForDrawables")
    override fun onBindViewHolder(holder: DirectoryViewHolder, position: Int) {
        val currentFile = dataList[position]
        val fileType = FileTypeUtils.getFileType(currentFile)
        holder.mFileImage.setImageResource(fileType.icon)
        val fileName = currentFile.file.name

        holder.mFileSubtitle.setText(fileType.description)
        if (fileType == FileTypeUtils.FileType.DIRECTORY) {
            holder.mFileTitle.text = fileName
            holder.mNextImage.visibility = View.VISIBLE
            holder.mPhotoImage.visibility = View.GONE
            holder.mLabelImage.visibility = View.GONE
            holder.mFileSubtitle.text = "对象：" + currentFile.file?.listFiles()?.size
        } else {
            holder.mFileTitle.text = fileName.substring(FileTypeUtils.getCheckTypeStr(fileType).length, fileName.length)
            if (currentFile.isHasPhoto && !currentFile.isToChooseModel) {
                holder.mPhotoImage.visibility = View.VISIBLE
            } else {
                holder.mPhotoImage.visibility = View.GONE
            }
            if (!currentFile.isToChooseModel) {
                holder.mLabelImage.visibility = View.VISIBLE
            } else {
                holder.mLabelImage.visibility = View.GONE
                if (TextUtils.isEmpty(currentFile.marks)) {
                    holder.mLabelImage.setImageDrawable(MRApplication.instance.getDrawable(R.mipmap.data_icon_mark_nor))
                } else {
                    holder.mLabelImage.setImageDrawable(MRApplication.instance.getDrawable(R.mipmap.data_icon_mark_sel))
                }
            }
            holder.mNextImage.visibility = View.VISIBLE
            holder.mFileSubtitle.text =
                MRApplication.instance.resources.getText(fileType.description)
        }
        if (currentFile.isToChooseModel) {
            holder.mSelectButton.visibility = View.VISIBLE
            holder.mNextImage.visibility = View.GONE
        } else {
            holder.mSelectButton.visibility = View.GONE
            holder.mNextImage.visibility = View.VISIBLE
        }
        if (currentFile.isSelect) {
            holder.mSelectButton.setImageDrawable(MRApplication.instance.getDrawable(R.mipmap.data_icon_tick_sel))
        } else {
            holder.mSelectButton.setImageDrawable(MRApplication.instance.getDrawable(R.mipmap.data_icon_tick_nor))
        }
        holder.layout.setTag(R.id.tag_position, position)
        holder.layout.setOnClickListener {
            val p = it.getTag(R.id.tag_position) as Int
            if (currentFile.isToChooseModel) {
                dataList[p].isSelect = !dataList[p].isSelect
                notifyItemChanged(p)
                return@setOnClickListener
            }
            mOnItemClickListener?.onItemClick(it, p)
        }
        holder.mSelectButton.setTag(R.id.tag_position, position)
        holder.mSelectButton.setOnClickListener {
            val p = it.getTag(R.id.tag_position) as Int
            dataList[p].isSelect = !dataList[p].isSelect
            notifyItemChanged(p)
        }
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    fun getModel(index: Int): CheckDataFileModel {
        return dataList[index]
    }

    internal class DirectoryViewHolder(
        itemView: View,
        photoClickListener: OnItemPhotoClickListener?,
        labelClickListener: OnItemLabelClickListener?
    ) :
        RecyclerView.ViewHolder(itemView) {
        val layout: LinearLayout
        val mFileImage: ImageView
        val mPhotoImage: ImageView
        val mLabelImage: ImageView
        val mNextImage: ImageView
        val mFileTitle: TextView
        val mFileSubtitle: TextView
        val mSelectButton: ImageButton

        init {
            layout = itemView.findViewById(R.id.layout)
            mFileImage = itemView.findViewById(R.id.item_file_image)
            mPhotoImage = itemView.findViewById(R.id.photoIv)
            mLabelImage = itemView.findViewById(R.id.labelIv)
            mNextImage = itemView.findViewById(R.id.iconNextIv)

            mFileTitle = itemView.findViewById(R.id.item_file_title)
            mFileSubtitle = itemView.findViewById(R.id.item_file_subtitle)
            mSelectButton = itemView.findViewById(R.id.selectButton)
            mPhotoImage.setOnClickListener { v ->
                photoClickListener?.onItemClick(
                    v,
                    adapterPosition
                )
            }
            mLabelImage.setOnClickListener { v ->
                labelClickListener?.onItemClick(
                    v,
                    adapterPosition
                )
            }
        }
    }
}