package com.mr.mf_pd.application.view.file

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.input.input
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.afollestad.materialdialogs.list.listItems
import com.mr.mf_pd.application.R
import com.mr.mf_pd.application.app.MRApplication
import com.mr.mf_pd.application.common.ConstantStr
import com.mr.mf_pd.application.databinding.FileListDataBinding
import com.mr.mf_pd.application.utils.FileUtils
import com.mr.mf_pd.application.view.base.AbsBaseActivity
import com.mr.mf_pd.application.view.data.FileDataActivity

import com.mr.mf_pd.application.view.file.DirectoryFragment.FileClickListener
import com.mr.mf_pd.application.view.file.filter.FileFilter
import com.mr.mf_pd.application.view.file.filter.PatternFilter
import com.mr.mf_pd.application.view.file.listener.DirectoryListener
import com.mr.mf_pd.application.view.file.model.CheckDataFileModel
import kotlinx.android.synthetic.main.activity_file_picker.*
import java.io.File
import java.util.*
import java.util.regex.Pattern
import kotlin.collections.ArrayList

class FilePickerActivity : AbsBaseActivity<FileListDataBinding>(), FileClickListener {

    private var mToolbar: Toolbar? = null
    private var mStart = MRApplication.instance.fileCacheFile()
    private var mCurrent = mStart
    private var mTitle: CharSequence? = null
    private var mCloseable = true
    private var mChooseDir = false
    private var mFilter: FileFilter? = null

    private var mFileTypeTv: TextView? = null
    private lateinit var mActionButton: Button
    private var cutFiles: ArrayList<CheckDataFileModel> = ArrayList()

    private var directoryListeners: ArrayList<DirectoryListener> = ArrayList()
    private var currentActionType = ActionType.NULL
    private var updateDirectoryListener: UpdateDirectoryListener? = null

    enum class ActionType {
        Delete, Cut, Paste, NULL
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        mChooseDir = intent.getBooleanExtra(ConstantStr.KEY_BUNDLE_BOOLEAN, false)
        super.onCreate(savedInstanceState)
    }

    private fun initArguments(savedInstanceState: Bundle?) {
        if (intent.hasExtra(ARG_FILTER)) {
            val filter = intent.getSerializableExtra(ARG_FILTER)
            mFilter = if (filter is Pattern) {
                PatternFilter(filter, false)
            } else {
                filter as FileFilter
            }
        }
        if (savedInstanceState != null) {
            mStart = savedInstanceState.getSerializable(STATE_START_FILE) as File?
            mCurrent = savedInstanceState.getSerializable(STATE_CURRENT_FILE) as File?
            updateTitle()
        } else {
            if (intent.hasExtra(ARG_START_FILE)) {
                mStart = intent.getSerializableExtra(ARG_START_FILE) as File
                mCurrent = mStart
            }
            if (intent.hasExtra(ARG_CURRENT_FILE)) {
                val currentFile = intent.getSerializableExtra(ARG_CURRENT_FILE) as File
                if (FileUtils.isParent(currentFile, mStart)) {
                    mCurrent = currentFile
                }
            }
        }
        if (intent.hasExtra(ARG_TITLE)) {
            mTitle = intent.getCharSequenceExtra(ARG_TITLE)
        }
        if (intent.hasExtra(ARG_CLOSEABLE)) {
            mCloseable = intent.getBooleanExtra(ARG_CLOSEABLE, true)
        }
    }


    private fun initBackStackState() {
        val path: MutableList<File?> = ArrayList()
        var current = mCurrent
        while (current != null) {
            path.add(current)
            if (current == mStart) {
                break
            }
            current = FileUtils.getParentOrNull(current)
        }
        path.reverse()
        for (file in path) {
            addFragmentToBackStack(file)
        }
    }

    private fun updateTitle() {
        val homeDir = mStart?.absolutePath
        if (homeDir != null && mCurrent != null) {
            val titlePath = mCurrent!!.absolutePath.substring(homeDir.length)
            var dirStr = "/"
            if (!TextUtils.isEmpty(titlePath)) {
                dirStr = titlePath
            }
            filePathTv.text = dirStr
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if (mChooseDir) {
            menuInflater.inflate(R.menu.menu_choose_dir, menu)
        } else {
            menuInflater.inflate(R.menu.menu_file, menu)
        }
        return true
    }

    override fun onOptionsItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            android.R.id.home -> {
                onBackPressed()
            }
            R.id.action_choose -> {
                val intent = Intent()
                intent.putExtra(ConstantStr.KEY_BUNDLE_STR, mCurrent?.absolutePath)
                setResult(Activity.RESULT_OK, intent)
                finish()
            }
            R.id.action_close -> {
                finish()
            }
            R.id.menu_new_dir -> {
                MaterialDialog(this).show {
                    setTheme(R.style.AppTheme_MaterialDialog)
                    title(text = "请输入文件夹名称")
                    input { _, text ->
                        mCurrent?.let {
                            val file = File(it, text.toString())
                            if (file.mkdir()) {
                                updateDirectoryListener?.updateFiles()
                            }
                        }
                    }
                    negativeButton(res = R.string.cancel, click = {
                        it.dismiss()
                    })
                    lifecycleOwner(this@FilePickerActivity)
                }
            }
            R.id.menu_edit_dir -> {
                if (mCurrent != null && mStart != null && !mCurrent!!.absolutePath.equals(mStart!!.absolutePath)) {
                    val dirName = mCurrent?.name
                    MaterialDialog(this).show {
                        setTheme(R.style.AppTheme_MaterialDialog)
                        title(text = "请输入文件夹名称")
                        input(prefill = dirName) { _, text ->
                            mCurrent?.let {
                                val file = File(it.parentFile, text.toString())
                                if (it.renameTo(file)) {
                                    mCurrent = file
                                    updateTitle()
                                }
                            }
                        }
                        negativeButton(res = R.string.cancel, click = {
                            it.dismiss()
                        })
                        lifecycleOwner(this@FilePickerActivity)
                    }
                }
            }
            R.id.menu_cut -> {//剪贴
                currentActionType = ActionType.Cut
                startDealAction()
            }
            R.id.menu_paste -> {//粘贴
                currentActionType = ActionType.Paste
                startDealAction()
                finishDealAction(true)
            }
            R.id.menu_delete -> {//删除
                currentActionType = ActionType.Delete
                startDealAction()
            }
            R.id.menu_share -> {

            }
        }
        return super.onOptionsItemSelected(menuItem)
    }

    private fun addFragmentToBackStack(file: File?) {
        val fragment = DirectoryFragment.getInstance(
            file,
            mFilter
        )
        updateDirectoryListener = fragment
        directoryListeners.add(fragment)
        supportFragmentManager
            .beginTransaction()
            .replace(
                R.id.container,
                fragment
            )
            .addToBackStack(null)
            .commit()
    }

    override fun onBackAction() {
        when {
            supportFragmentManager.backStackEntryCount > 1 -> {
                supportFragmentManager.popBackStack()
                mCurrent = FileUtils.getParentOrNull(mCurrent)
                updateTitle()
            }
            currentActionType != ActionType.NULL -> {
                finishDealAction()
            }
            else -> {
                setResult(RESULT_CANCELED)
                finish()
            }
        }
    }

    override fun getToolBarTitle(): String {
        return "检测数据"
    }

    public override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putSerializable(STATE_CURRENT_FILE, mCurrent)
        outState.putSerializable(STATE_START_FILE, mStart)
    }

    override fun onFileClicked(clickedFile: CheckDataFileModel?) {
        Handler().postDelayed({ handleFileClicked(clickedFile) }, HANDLE_CLICK_DELAY.toLong())
    }

    private fun handleFileClicked(clickedFile: CheckDataFileModel?) {
        if (isFinishing) {
            setResultAndFinish(clickedFile?.file)
            return
        }
        if (clickedFile != null) {
            if (clickedFile.isCheckFile) {
                val intent = Intent(this, FileDataActivity::class.java)
                intent.putExtra(ConstantStr.KEY_BUNDLE_STR, clickedFile.file.absolutePath)
                startActivity(intent)
            } else {
                mCurrent = clickedFile.file
                addFragmentToBackStack(clickedFile.file)
                updateTitle()
            }
        }

    }

    private fun setResultAndFinish(file: File?) {
        val data = Intent()
        data.putExtra(RESULT_FILE_PATH, file!!.path)
        setResult(RESULT_OK, data)
        finish()
    }

    companion object {
        const val ARG_START_FILE = "arg_start_path"
        const val ARG_CURRENT_FILE = "arg_current_path"
        const val ARG_FILTER = "arg_filter"
        const val ARG_CLOSEABLE = "arg_closeable"
        const val ARG_TITLE = "arg_title"
        const val STATE_START_FILE = "state_start_path"
        private const val STATE_CURRENT_FILE = "state_current_path"
        const val RESULT_FILE_PATH = "result_file_path"
        private const val HANDLE_CLICK_DELAY = 150
    }

    override fun initView(savedInstanceState: Bundle?) {
        mToolbar = findViewById(R.id.toolbar)
        findViewById<View>(R.id.toolbar_div).visibility = View.GONE
        if (savedInstanceState == null) {
            initBackStackState()
        }
        mFileTypeTv = findViewById(R.id.fileTypeTv)
        findViewById<RelativeLayout>(R.id.chooseFileTypeLayout).setOnClickListener {
            MaterialDialog(this)
                .show {
                    setTheme(R.style.AppTheme_MaterialDialog)
                    listItems(R.array.choose_file_type) { _, index, text ->
                        mFileTypeTv?.text = text
                        directoryListeners.forEach {
                            it.onFileTypeChange(index)
                        }
                    }
                    lifecycleOwner(this@FilePickerActivity)
                }
        }
        mActionButton = findViewById(R.id.actionButton)
        mActionButton.setOnClickListener {
            MaterialDialog(this).show {
                setTheme(R.style.AppTheme_MaterialDialog)
                when (currentActionType) {
                    ActionType.Delete -> {
                        title(text = "是否删除选中的文件?")
                    }
                    ActionType.Cut -> {
                        title(text = "是否剪贴选中的文件?")
                    }
                    else -> {
                        this.dismiss()
                    }
                }
                negativeButton {
                    it.dismiss()
                    finishDealAction()
                }
                positiveButton {
                    it.dismiss()
                    finishDealAction(true)
                }
            }
        }
    }

    private fun startDealAction() {
        when (currentActionType) {
            ActionType.Delete -> {
                mActionButton.visibility = View.VISIBLE
                mActionButton.text = "删除"
            }
            ActionType.Cut -> {
                mActionButton.visibility = View.VISIBLE
                mActionButton.text = "剪贴"
            }
            else -> {
                mActionButton.visibility = View.GONE
            }
        }
        updateDirectoryListener?.updateDirectory(currentActionType)
    }


    private fun finishDealAction(hasAction: Boolean = false) {
        mActionButton.visibility = View.GONE
        if (hasAction && updateDirectoryListener != null) {
            when (currentActionType) {
                ActionType.Delete -> {
                    val fileList = ArrayList<File>()
                    updateDirectoryListener!!.getSelectData().map {
                        fileList.add(it.file)
                    }
                    FileUtils.deleteFiles(fileList, object :
                        FileUtils.FileActionListener {
                        override fun onSuccess() {
                            updateDirectoryListener?.updateFiles()
                        }

                        override fun onFail() {
                            Toast.makeText(this@FilePickerActivity, "文件删除失败", Toast.LENGTH_SHORT)
                                .show()
                        }
                    })
                }
                ActionType.Cut -> {
                    cutFiles.clear()
                    cutFiles.addAll(updateDirectoryListener!!.getSelectData())
                    Toast.makeText(this, "剪贴成功", Toast.LENGTH_SHORT).show()
                }
                ActionType.Paste -> {
                    if (cutFiles.isNotEmpty() && mCurrent != null) {
                        val fileList = ArrayList<File>()
                        cutFiles.map {
                            fileList.add(it.file)
                        }
                        FileUtils.cutFiles(
                            mCurrent!!,
                            fileList,
                            object : FileUtils.FileActionListener {

                                override fun onSuccess() {
                                    FileUtils.deleteFiles(fileList,
                                        object : FileUtils.FileActionListener {
                                            override fun onSuccess() {
                                                cutFiles.clear()
                                                updateDirectoryListener?.updateFiles()
                                            }

                                            override fun onFail() {

                                            }
                                        })
                                }

                                override fun onFail() {

                                }
                            })
                    } else {
                        Toast.makeText(this, "没有选择中文件", Toast.LENGTH_SHORT).show()
                    }
                }
                else -> {

                }
            }
        }
        currentActionType = ActionType.NULL
        updateDirectoryListener?.updateDirectory(currentActionType)
    }

    override fun initData(savedInstanceState: Bundle?) {
        initArguments(savedInstanceState)
    }

    override fun getContentView(): Int {
        return R.layout.activity_file_picker
    }
}