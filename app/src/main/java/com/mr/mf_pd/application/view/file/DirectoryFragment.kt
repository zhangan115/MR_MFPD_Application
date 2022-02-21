package com.mr.mf_pd.application.view.file

import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.mr.mf_pd.application.R
import com.mr.mf_pd.application.utils.FileTypeUtils
import com.mr.mf_pd.application.utils.FileUtils
import com.mr.mf_pd.application.view.file.adapter.DirectoryAdapter
import com.mr.mf_pd.application.view.file.filter.FileFilter
import com.mr.mf_pd.application.view.file.listener.DirectoryListener
import com.mr.mf_pd.application.view.file.listener.LabelClickListener
import com.mr.mf_pd.application.view.file.listener.PhotoClickListener
import com.mr.mf_pd.application.view.file.listener.ThrottleClickListener
import com.mr.mf_pd.application.view.file.model.CheckDataFileModel
import com.mr.mf_pd.application.widget.EmptyRecyclerView
import java.io.File

class DirectoryFragment : Fragment(), DirectoryListener, UpdateDirectoryListener {

    private var mEmptyView: View? = null
    private var isDelete: Boolean = false
    private var fileType: Int = 0
    private var mFile: File? = null
    private var mFilter: FileFilter? = null
    private var mDirectoryRecyclerView: EmptyRecyclerView? = null
    private var mDirectoryAdapter: DirectoryAdapter? = null
    private var mFileClickListener: FileClickListener? = null

    private var checkDataFileModels: ArrayList<CheckDataFileModel> = ArrayList()

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
        mDirectoryAdapter = DirectoryAdapter(checkDataFileModels)
        mDirectoryAdapter?.setOnItemClickListener(object : ThrottleClickListener() {

            override fun onItemClickThrottled(view: View?, position: Int) {
                if (mFileClickListener != null) {
                    mFileClickListener!!.onFileClicked(mDirectoryAdapter!!.getModel(position))
                }
            }
        })
        mDirectoryAdapter?.setOnPhotoClickListener(object : PhotoClickListener() {
            override fun onItemClickThrottled(view: View?, position: Int) {

            }
        })
        mDirectoryAdapter?.setOnLabelClickListener(object : LabelClickListener() {
            override fun onItemClickThrottled(view: View?, position: Int) {
                showCheckDataNoteDialog(checkDataFileModels[position])
            }
        })
        mDirectoryRecyclerView?.layoutManager = LinearLayoutManager(activity)
        mDirectoryRecyclerView?.adapter = mDirectoryAdapter
        mDirectoryRecyclerView?.setEmptyView(mEmptyView)

        FileUtils.getFileList(mFile, FileTypeUtils.FileType.DIRECTORY) {
            this.checkDataFileModels.clear()
            this.checkDataFileModels.addAll(it)
            this.mDirectoryAdapter?.notifyDataSetChanged()
        }
    }

    private fun initArgs() {
        val arguments = arguments
        if (arguments != null && arguments.containsKey(ARG_FILE)) {
            mFile = arguments.getSerializable(ARG_FILE) as File?
        }
        mFilter = arguments?.getSerializable(ARG_FILTER) as FileFilter?
    }

    internal interface FileClickListener {

        fun onFileClicked(clickedFile: CheckDataFileModel?)

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

    override fun onFileTypeChange(fileType: Int) {
        this.fileType = fileType
        initFilesList()
    }

    override fun deleteModel(isDelete: Boolean) {
        this.isDelete = isDelete
        initFilesList()
    }

    /**
     * 显示标记的Dialog
     */
    private fun showCheckDataNoteDialog(model: CheckDataFileModel?) {
        MaterialDialog(requireActivity()).show {
            setContentView(R.layout.dialog_check_data_note)
            val radioGroup = findViewById<RadioGroup>(R.id.chooseColorRg)
            val noteEt = findViewById<EditText>(R.id.noteEt)
            if (model != null) {
                if (!TextUtils.isEmpty(model.marks)) {
                    noteEt.setText(model.marks)
                }
                if (model.color != -1) {
                    radioGroup.check(radioGroup.getChildAt(model.color).id)
                }
            }
            findViewById<TextView>(R.id.cancelTv).setOnClickListener {
                dismiss()
            }
            findViewById<TextView>(R.id.saveTv).setOnClickListener {
                val rb: RadioButton = findViewById(radioGroup.checkedRadioButtonId)
                val tag: String = rb.tag as String
                model?.marks = noteEt.text.toString()
                model?.color = tag.toInt()
                dismiss()
            }
        }
    }

    override fun updateDirectory(action: FilePickerActivity.ActionType) {
        if (action == FilePickerActivity.ActionType.NULL || action == FilePickerActivity.ActionType.Paste) {
            checkDataFileModels.forEach { model ->
                model.isSelect = false
                model.isToChooseModel = false
            }
        } else {
            checkDataFileModels.forEach { model ->
                model.isSelect = false
                model.isToChooseModel = true
            }
        }
        mDirectoryAdapter?.notifyDataSetChanged()
    }

    override fun getSelectData(): List<CheckDataFileModel> {
        return checkDataFileModels.filter { it.isSelect }

    }

    override fun updateFiles() {
        initFilesList()
    }


}