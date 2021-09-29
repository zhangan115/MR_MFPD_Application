package com.mr.mf_pd.application.view.file.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mr.mf_pd.application.R
import com.mr.mf_pd.application.utils.FileTypeUtils
import com.mr.mf_pd.application.view.file.listener.OnItemClickListener
import com.mr.mf_pd.application.view.file.adapter.DirectoryAdapter.DirectoryViewHolder
import java.io.File

internal class DirectoryAdapter(private val mFiles: List<File>) :
    RecyclerView.Adapter<DirectoryViewHolder>() {
    private var mOnItemClickListener: OnItemClickListener? = null
    fun setOnItemClickListener(listener: OnItemClickListener?) {
        mOnItemClickListener = listener
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DirectoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_file, parent, false)
        return DirectoryViewHolder(view, mOnItemClickListener)
    }

    override fun onBindViewHolder(holder: DirectoryViewHolder, position: Int) {
        val currentFile = mFiles[position]
        val fileType = FileTypeUtils.getFileType(currentFile)
        holder.mFileImage.setImageResource(fileType.icon)
        holder.mFileSubtitle.setText(fileType.description)
        holder.mFileTitle.text = currentFile.name
    }

    override fun getItemCount(): Int {
        return mFiles.size
    }

    fun getModel(index: Int): File {
        return mFiles[index]
    }

    internal class DirectoryViewHolder(itemView: View, clickListener: OnItemClickListener?) :
        RecyclerView.ViewHolder(itemView) {
        val mFileImage: ImageView
        val mFileTitle: TextView
        val mFileSubtitle: TextView

        init {
            itemView.setOnClickListener { v -> clickListener!!.onItemClick(v, adapterPosition) }
            mFileImage = itemView.findViewById(R.id.item_file_image)
            mFileTitle = itemView.findViewById(R.id.item_file_title)
            mFileSubtitle = itemView.findViewById(R.id.item_file_subtitle)
        }
    }
}