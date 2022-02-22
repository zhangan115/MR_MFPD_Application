package com.mr.mf_pd.application.view.file

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.google.gson.Gson
import com.mr.mf_pd.application.R
import com.mr.mf_pd.application.common.ConstantInt
import com.mr.mf_pd.application.common.ConstantStr
import com.mr.mf_pd.application.databinding.DirectoryDataBinding
import com.mr.mf_pd.application.utils.FileTypeUtils
import com.mr.mf_pd.application.utils.FileUtils
import com.mr.mf_pd.application.view.base.BaseFragment
import com.mr.mf_pd.application.view.base.ext.choosePhoto
import com.mr.mf_pd.application.view.base.ext.takePhoto
import com.mr.mf_pd.application.view.file.adapter.DirectoryAdapter
import com.mr.mf_pd.application.view.file.filter.FileFilter
import com.mr.mf_pd.application.view.file.listener.DirectoryListener
import com.mr.mf_pd.application.view.file.listener.LabelClickListener
import com.mr.mf_pd.application.view.file.listener.PhotoClickListener
import com.mr.mf_pd.application.view.file.listener.ThrottleClickListener
import com.mr.mf_pd.application.view.file.model.CheckConfigModel
import com.mr.mf_pd.application.view.file.model.CheckDataFileModel
import com.mr.mf_pd.application.widget.EmptyRecyclerView
import kotlinx.coroutines.GlobalScope
import java.io.File

class DirectoryFragment : BaseFragment<DirectoryDataBinding>(), DirectoryListener,
    UpdateDirectoryListener {

    private var mEmptyView: View? = null
    private var mFileType: FileTypeUtils.FileType = FileTypeUtils.FileType.DIRECTORY
    private var currentCheckData: CheckDataFileModel? = null
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
                val intent = Intent(this@DirectoryFragment.activity, PhotoListActivity::class.java)
                intent.putExtra(
                    ConstantStr.KEY_BUNDLE_STR,
                    checkDataFileModels[position].file.absolutePath
                )
                startActivity(intent)
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

        updateData(mFileType)
    }

    private fun updateData(fileType: FileTypeUtils.FileType) {
        FileUtils.getFileList(mFile, fileType) {
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
        mFileType = arguments?.getSerializable(ARG_FILE_TYPE) as FileTypeUtils.FileType
    }

    internal interface FileClickListener {

        fun onFileClicked(clickedFile: CheckDataFileModel?)

    }

    companion object {

        private const val ARG_FILE = "arg_file_path"
        private const val ARG_FILTER = "arg_filter"
        private const val ARG_FILE_TYPE = "file_type"

        @JvmStatic
        fun getInstance(
            file: File?,
            filter: FileFilter?,
            fileType: FileTypeUtils.FileType
        ): DirectoryFragment {
            val instance = DirectoryFragment()
            val args = Bundle()
            args.putSerializable(ARG_FILE, file)
            args.putSerializable(ARG_FILTER, filter)
            args.putSerializable(ARG_FILE_TYPE, fileType)
            instance.arguments = args
            return instance
        }
    }

    override fun onFileTypeChange(fileType: FileTypeUtils.FileType) {
        this.mFileType = fileType
        updateData(fileType)
    }

    /**
     * 显示标记的Dialog
     */
    private fun showCheckDataNoteDialog(model: CheckDataFileModel?) {
        currentCheckData = model
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
            findViewById<ImageView>(R.id.takePhoto).setOnClickListener {
                takePhoto(ConstantInt.ACTION_TAKE_PHOTO)
            }
            findViewById<ImageView>(R.id.checkPhoto).setOnClickListener {
                choosePhoto(ConstantInt.ACTION_CHOOSE_FILE)
            }
            findViewById<TextView>(R.id.saveTv).setOnClickListener {
                val rb: RadioButton = findViewById(radioGroup.checkedRadioButtonId)
                val tag: String = rb.tag as String
                model?.marks = noteEt.text.toString()
                model?.color = tag.toInt()
                model?.let {
                    GlobalScope.run {
                        val configFile = File(it.file, ConstantStr.CHECK_FILE_CONFIG)
                        if (!configFile.exists()) {
                            configFile.createNewFile()
                        }
                        val checkConfig = CheckConfigModel()
                        checkConfig.color = it.color
                        checkConfig.marks = it.marks
                        FileUtils.writeStr2File(Gson().toJson(checkConfig), configFile)
                    }
                }
                mDirectoryRecyclerView?.adapter?.notifyDataSetChanged()
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
        updateData(mFileType)
    }

    override fun lazyLoad() {

    }

    override fun getContentView(): Int {
        return R.layout.fragment_directory
    }

    override fun initData() {

    }

    override fun createTargetDir(): String? {
        return currentCheckData?.file?.absolutePath
    }


    override fun initView() {
        mDirectoryRecyclerView = view?.findViewById(R.id.directory_recycler_view)
        mEmptyView = view?.findViewById(R.id.directory_empty_view)
    }

    override fun setViewModel(dataBinding: DirectoryDataBinding?) {

    }

    override fun dealFile(requestCode: Int, file: File) {
        super.dealFile(requestCode, file)
        currentCheckData?.isHasPhoto = true
        mDirectoryRecyclerView?.adapter?.notifyDataSetChanged()
    }

}