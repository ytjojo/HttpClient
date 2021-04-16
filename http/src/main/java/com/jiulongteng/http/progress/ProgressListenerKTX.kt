package com.jiulongteng.http.progress

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch


/**
 * Correspond to [ProgressListener.update]
 */
typealias onUploadProgress =  (
         progress: Int, currentSize: Long, contentLength: Long
) -> Unit

fun createUploadProgressListener(
        scope:CoroutineScope,
        onUploadProgress: onUploadProgress
): ProgressListener {
    return ProgressListener { progress, currentSize, totalSize ->
        scope.launch {
            onUploadProgress.invoke(progress,currentSize,totalSize)
        }

    }
}