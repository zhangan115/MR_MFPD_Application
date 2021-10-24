package com.mr.mf_pd.application.view.file.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mr.mf_pd.application.R
import com.mr.mf_pd.application.utils.FileTypeUtils
import com.mr.mf_pd.application.view.file.listener.OnItemClickListener
import com.mr.mf_pd.application.view.file.adapter.DirectoryAdapter.DirectoryViewHolder
import com.mr.mf_pd.application.view.file.listener.OnItemLabelClickListener
import com.mr.mf_pd.application.view.file.listener.OnItemPhotoClickListener
import java.io.File

internal class DirectoryAdapter(private val mFiles: List<File>) :
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
            mOnItemClickListener,
            mOnPhotoClickListener,
            mOnLabelClickListener
        )
    }

    override fun onBindViewHolder(holder: DirectoryViewHolder, position: Int) {
        val currentFile = mFiles[position]
        val fileType = FileTypeUtils.getFileType(currentFile)
        holder.mFileImage.setImageResource(fileType.icon)
        holder.mFileTitle.text = currentFile.name
        holder.mFileSubtitle.setText(fileType.description)
        if (fileType == FileTypeUtils.FileType.DIRECTORY) {
            holder.mNextImage.visibility = View.VISIBLE
            holder.mPhotoImage.visibility = View.GONE
            holder.mLabelImage.visibility = View.GONE
        } else {
            holder.mNextImage.visibility = View.GONE

            holder.mPhotoImage.visibility = View.VISIBLE
            holder.mLabelImage.visibility = View.VISIBLE
        }
    }

    override fun getItemCount(): Int {
        return mFiles.size
    }

    fun getModel(index: Int): File {
        return mFiles[index]
    }

    internal class DirectoryViewHolder(
        itemView: View,
        clickListener: OnItemClickListener?,
        photoClickListener: OnItemPhotoClickListener?,
        labelClickListener: OnItemLabelClickListener?
    ) :
        RecyclerView.ViewHolder(itemView) {
        val mFileImage: ImageView
        val mPhotoImage: ImageView
        val mLabelImage: ImageView
        val mNextImage: ImageView
        val isDeleteCb: CheckBox
        val mFileTitle: TextView
        val mFileSubtitle: TextView

        init {
            itemView.setOnClickListener { v -> clickListener?.onItemClick(v, adapterPosition) }
            mFileImage = itemView.findViewById(R.id.item_file_image)
            mPhotoImage = itemView.findViewById(R.id.photoIv)
            mLabelImage = itemView.findViewById(R.id.labelIv)
            mNextImage = itemView.findViewById(R.id.iconNextIv)
            isDeleteCb = itemView.findViewById(R.id.deleteCb)
            mFileTitle = itemView.findViewById(R.id.item_file_title)
            mFileSubtitle = itemView.findViewById(R.id.item_file_subtitle)
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