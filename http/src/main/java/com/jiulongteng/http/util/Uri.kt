@file:JvmName("UriUtil")

package com.jiulongteng.http.util

import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import com.jiulongteng.http.download.db.DownloadCache
import com.jiulongteng.http.progress.ProgressListener
import com.jiulongteng.http.progress.ProgressRequestBody
import com.jiulongteng.http.progress.UriRequestBody
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.Response
import java.io.File
import java.io.FileNotFoundException

/**
 * User: ljx
 * Date: 2020/9/26
 * Time: 14:55
 */

@JvmOverloads
fun Uri.asRequestBody(
        contentType: MediaType? = null
): RequestBody = UriRequestBody(contentType, this)

@JvmOverloads
fun Uri.asPart(
        key: String,
        filename: String? = null,
        contentType: MediaType? = null
): MultipartBody.Part {
    val newFilename = filename ?: displayName()
    return MultipartBody.Part.createFormData(key, newFilename, asRequestBody(contentType))
}

@JvmOverloads
fun Uri.asPart(
        key: String,
        filename: String? = null,
        contentType: MediaType? = null,
        progressListener: ProgressListener?
): MultipartBody.Part {
    val newFilename = filename ?: displayName()
    return MultipartBody.Part.createFormData(key, newFilename, ProgressRequestBody(asRequestBody(contentType),progressListener))
}

fun Uri?.length(context: Context): Long {
    return length(context.contentResolver)
}

//return The size of the media item, return -1 if does not exist, might block.
fun Uri?.length(contentResolver: ContentResolver): Long {
    if (this == null) return -1L
    if (scheme == ContentResolver.SCHEME_FILE) {
        return File(path).length()
    }
    return try {
        contentResolver.openFileDescriptor(this, "r")?.statSize ?: -1L
    } catch (e: FileNotFoundException) {
        -1L
    }
}

internal fun Uri.displayName(): String? {
    if (scheme == ContentResolver.SCHEME_FILE) {
        return lastPathSegment
    }
    return getColumnValue(DownloadCache.getContext().contentResolver, MediaStore.MediaColumns.DISPLAY_NAME)
}

//Return the value of the specified column，return null if does not exist
internal fun Uri.getColumnValue(contentResolver: ContentResolver, columnName: String): String? {
    return contentResolver.query(this, arrayOf(columnName),
            null, null, null)?.use {
        if (it.moveToFirst()) it.getString(0) else null
    }
}

//find the Uri by filename and relativePath, return null if find fail.  RequiresApi 29
fun Uri.query(context: Context, filename: String?, relativePath: String?): Uri? {
    if (filename.isNullOrEmpty() || relativePath.isNullOrEmpty()) return null
    val realRelativePath = relativePath.let {
        //Remove the prefix slash if it exists
        if (it.startsWith("/")) it.substring(1) else it
    }.let {
        //Suffix adds a slash if it does not exist
        if (it.endsWith("/")) it else "$it/"
    }
    val columnNames = arrayOf(
            MediaStore.MediaColumns._ID
    )
    return context.contentResolver.query(this, columnNames,
            "relative_path=? AND _display_name=?", arrayOf(realRelativePath, filename), null)?.use {
        if (it.moveToFirst()) {
            val uriId = it.getLong(0)
            ContentUris.withAppendedId(this, uriId)
        } else null
    }
}

/**
 *  @param relativePath  文件相对路径，可取值:
 * [Environment.DIRECTORY_DOWNLOADS]
 * [Environment.DIRECTORY_DCIM]
 * [Environment.DIRECTORY_PICTURES]
 * [Environment.DIRECTORY_MUSIC]
 * [Environment.DIRECTORY_MOVIES]
 * [Environment.DIRECTORY_DOCUMENTS]
 * @param rootUri
 * [MediaStore.Files.getContentUri]
 * [MediaStore.Downloads.EXTERNAL_CONTENT_URI]
 * [MediaStore.Audio.Media.EXTERNAL_CONTENT_URI]
 * [MediaStore.Video.Media.EXTERNAL_CONTENT_URI]
 * [MediaStore.Images.Media.EXTERNAL_CONTENT_URI]
 */
fun insert(response: Response, rootUri: Uri, filename: String, relativePath: String, isAndroidQ: Boolean): Uri {
    return if (isAndroidQ) {
        val uri = rootUri.query(DownloadCache.getContext(), filename, relativePath)
        /*
         * 通过查找，要插入的Uri已经存在，就无需再次插入
         * 否则会出现新插入的文件，文件名被系统更改的现象，因为insert不会执行覆盖操作
         */
        if (uri != null) return uri
        ContentValues().run {
            put(MediaStore.MediaColumns.RELATIVE_PATH, relativePath) //下载到指定目录
            put(MediaStore.MediaColumns.DISPLAY_NAME, filename)   //文件名
            //取contentType响应头作为文件类型
            put(MediaStore.MediaColumns.MIME_TYPE, response.body()?.contentType().toString())
            DownloadCache.getContext().contentResolver.insert(rootUri, this)
            //当相同路径下的文件，在文件管理器中被手动删除时，就会插入失败
        } ?: throw NullPointerException("Uri insert failed. Try changing filename")
    } else {
        val file = File("${Environment.getExternalStorageDirectory()}/$relativePath/$filename")
        Uri.fromFile(file)
    }
}

fun Uri.delete(): Boolean {
    return if (scheme == ContentResolver.SCHEME_FILE) {
        File(path).delete()
    }else{
        DownloadCache.getContext().contentResolver.delete(this, null, null) > 0
    }
}