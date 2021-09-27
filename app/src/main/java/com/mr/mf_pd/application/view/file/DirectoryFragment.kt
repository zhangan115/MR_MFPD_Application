package com.mr.mf_pd.application.view.file

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.mr.mf_pd.application.R
import com.mr.mf_pd.application.utils.FileUtils
import com.mr.mf_pd.application.view.file.filter.FileFilter
import com.mr.mf_pd.application.widget.EmptyRecyclerView
import java.io.File

class DirectoryFragment : Fragment() {

    private var mEmptyView: View? = null
    private var mFile: File? = null
    private var mFilter: FileFilter? = null
    private var mDirectoryRecyclerView: EmptyRecyclerView? = null
    private var mDirectoryAdapter: DirectoryAdapter? = null
    private var mFileClickListener: FileClickListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mFileClickListener = context as FileClickListener
    }

    override fun onDetach() {
        super.onDetach()
        mFileClickListener = null
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_directory, container, false)
        mDirectoryRecyclerView = view.findViewById(R.id.directory_recycler_view)
        mEmptyView = view.findViewById(R.id.directory_empty_view)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initArgs()
        initFilesList()
    }

    private fun initFilesList() {
        mDirectoryAdapter = DirectoryAdapter(FileUtils.getFileList(mFile, mFilter))
        mDirectoryAdapter!!.setOnItemClickListener(object : ThrottleClickListener() {

            override fun onItemClickThrottled(view: View?, position: Int) {
                if (mFileClickListener != null) {
                    mFileClickListener!!.onFileClicked(mDirectoryAdapter!!.getModel(position))
                }
            }
        })
        mDirectoryRecyclerView!!.layoutManager = LinearLayoutManager(activity)
        mDirectoryRecyclerView!!.adapter = mDirectoryAdapter
        mDirectoryRecyclerView!!.setEmptyView(mEmptyView)
    }

    private fun initArgs() {
        val arguments = arguments
        if (arguments != null && arguments.containsKey(ARG_FILE)) {
            mFile = arguments.getSerializable(ARG_FILE) as File?
        }
        mFilter = arguments?.getSerializable(ARG_FILTER) as FileFilter?
        Log.d("za", "currentFile ${mFile?.absolutePath}")
    }

    internal interface FileClickListener {
        fun onFileClicked(clickedFile: File?)
    }

    companion object {
        private const val ARG_FILE = "arg_file_path"
        private const val ARG_FILTER = "arg_filter"

        @JvmStatic
        fun getInstance(
            file: File?,
            filter: FileFilter?
        ): DirectoryFragment {
            val instance = DirectoryFragment()
            val args = Bundle()
            args.putSerializable(ARG_FILE, file)
            args.putSerializable(ARG_FILTER, filter)
            instance.arguments = args
            return instance
        }
    }
}