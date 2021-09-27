package com.mr.mf_pd.application.view.file

import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.text.TextUtils
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import com.mr.mf_pd.application.R
import com.mr.mf_pd.application.app.MRApplication
import com.mr.mf_pd.application.databinding.FileListDataBinding
import com.mr.mf_pd.application.utils.FileUtils
import com.mr.mf_pd.application.view.base.AbsBaseActivity
import com.mr.mf_pd.application.view.file.DirectoryFragment.Companion.getInstance
import com.mr.mf_pd.application.view.file.DirectoryFragment.FileClickListener
import com.mr.mf_pd.application.view.file.filter.FileFilter
import com.mr.mf_pd.application.view.file.filter.PatternFilter
import java.io.File
import java.lang.reflect.Field
import java.util.*
import java.util.regex.Pattern

class FilePickerActivity : AbsBaseActivity<FileListDataBinding>(), FileClickListener {

    private var mToolbar: Toolbar? = null
    private var mStart = MRApplication.instance.fileCacheFile()
    private var mCurrent = mStart
    private var mTitle: CharSequence? = null
    private var mCloseable = true
    private var mFilter: FileFilter? = null

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
//        if (supportActionBar != null) {
//            val titlePath = mCurrent!!.absolutePath
//            if (TextUtils.isEmpty(mTitle)) {
//                supportActionBar!!.title = titlePath
//            } else {
//                supportActionBar!!.subtitle = titlePath
//            }
//        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_file, menu)
        return false
    }

    override fun onOptionsItemSelected(menuItem: MenuItem): Boolean {
        if (menuItem.itemId == android.R.id.home) {
            onBackPressed()
        } else if (menuItem.itemId == R.id.action_close) {
            finish()
        }
        return super.onOptionsItemSelected(menuItem)
    }

    private fun addFragmentToBackStack(file: File?) {
        supportFragmentManager
            .beginTransaction()
            .replace(
                R.id.container,
                getInstance(
                    file,
                    mFilter
                )
            )
            .addToBackStack(null)
            .commit()
    }

    override fun onBackAction() {
        if (supportFragmentManager.backStackEntryCount > 1) {
            supportFragmentManager.popBackStack()
            mCurrent = FileUtils.getParentOrNull(mCurrent)
            updateTitle()
        } else {
            setResult(RESULT_CANCELED)
            finish()
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

    override fun onFileClicked(clickedFile: File?) {
        Handler().postDelayed({ handleFileClicked(clickedFile) }, HANDLE_CLICK_DELAY.toLong())
    }

    private fun handleFileClicked(clickedFile: File?) {
        if (isFinishing) {
            return
        }
        //todo 文件点击了
        Log.d("za", "clickedFile is ${clickedFile?.absolutePath}")
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
        if (savedInstanceState == null) {
            initBackStackState()
        }
    }

    override fun initData(savedInstanceState: Bundle?) {
        initArguments(savedInstanceState)
    }

    override fun getContentView(): Int {
        return R.layout.activity_file_picker
    }
}