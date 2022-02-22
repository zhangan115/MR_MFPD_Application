package com.mr.mf_pd.application.utils

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import com.sito.tool.library.utils.luban.Luban
import com.sito.tool.library.utils.luban.OnCompressListener

import java.io.*

object PhotoCompressUtils {

    fun getFile(context: Context, file: File, listener: (File) -> Unit, errorListener: () -> Unit) {
        Luban.with(context).load(file).setCompressListener(object : OnCompressListener {
            override fun onSuccess(file: File?) {
                file?.let {
                    listener(it)
                }
            }

            override fun onError(e: Throwable?) {
                errorListener()
            }

            override fun onStart() {

            }
        }).launch()
    }

    fun getFile(context: Context, uri: Uri, listener: (File) -> Unit, errorListener: () -> Unit) {
        val file = from(context, uri)
        Luban.with(context).load(file).setCompressListener(object : OnCompressListener {
            override fun onSuccess(file: File?) {
                file?.let {
                    listener(it)
                }
            }

            override fun onError(e: Throwable?) {
                errorListener()
            }

            override fun onStart() {

            }
        }).launch()
    }

    fun getFile(context: Context,targetDir:String?, uri: Uri, listener: (File) -> Unit, errorListener: () -> Unit) {
        val file = from(context, uri)
        Luban.with(context).setTargetDir(targetDir).load(file).setCompressListener(object : OnCompressListener {
            override fun onSuccess(file: File?) {
                file?.let {
                    listener(it)
                }
            }

            override fun onError(e: Throwable?) {
                errorListener()
            }

            override fun onStart() {

            }
        }).launch()
    }


    @Throws(IOException::class)
    private fun from(context: Context, uri: Uri?): File? {
        val inputStream = context.contentResolver.openInputStream(uri!!)
        val fileName: String = getFileName(context, uri)!!
        val splitName: Array<String> = splitFileName(fileName)!!
        var tempFile = File.createTempFile(splitName[0], splitName[1])
        tempFile = rename(tempFile, fileName)
        tempFile.deleteOnExit()
        var out: FileOutputStream? = null
        try {
            out = FileOutputStream(tempFile)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
        if (inputStream != null && out != null) {
            copy(inputStream, out)
            inputStream.close()
        }
        out?.close()
        return tempFile
    }

    private fun rename(file: File, newName: String): File {
        val newFile = File(file.parent, newName)
        if (newFile != file) {
            if (newFile.exists()) {
                if (newFile.delete()) {
                    Log.d("FileUtil", "Delete old $newName file")
                }
            }
            if (file.renameTo(newFile)) {
                Log.d("FileUtil", "Rename file to $newName")
            }
        }
        return newFile
    }

    @Throws(IOException::class)
    fun copy(input: InputStream, output: OutputStream): Int {
        val count: Long = copyLarge(input, output)
        return if (count > Int.MAX_VALUE) {
            -1
        } else count.toInt()
    }

    @Throws(IOException::class)
    fun copyLarge(input: InputStream, output: OutputStream): Long {
        return copyLarge(input, output, ByteArray(DEFAULT_BUFFER_SIZE))
    }

    @Throws(IOException::class)
    fun copyLarge(
        input: InputStream,
        output: OutputStream,
        buffer: ByteArray?
    ): Long {
        var count: Long = 0
        var n: Int
        while (EOF != input.read(buffer).also { n = it }) {
            output.write(buffer, 0, n)
            count += n.toLong()
        }
        return count
    }

    private const val DEFAULT_BUFFER_SIZE = 1024 * 4
    private const val EOF = -1

    private fun splitFileName(fileName: String): Array<String>? {
        var name = fileName
        var extension = ""
        val i = fileName.lastIndexOf(".")
        if (i != -1) {
            name = fileName.substring(0, i)
            extension = fileName.substring(i)
        }
        return arrayOf(name, extension)
    }

    private fun getFileName(context: Context, uri: Uri): String? {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor =
                context.contentResolver.query(uri, null, null, null, null)
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                cursor?.close()
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result!!.lastIndexOf(File.separator)
            if (cut != -1) {
                result = result.substring(cut + 1)
            }
        }
        return result
    }
}