package com.jiulongteng.http.download

import com.jiulongteng.http.download.cause.EndCause

/**
 * Correspond to [DownloadListener.taskStart]
 */
typealias onTaskStart = (task: DownloadTask) -> Unit

/**
 * Correspond to [DownloadListener.connectTrialStart]
 */
typealias onConnectTrialStart = (
        task: DownloadTask
) -> Unit

/**
 * Correspond to [DownloadListener.fetchStart]
 */
typealias onFetchStart = (
        task: DownloadTask, isFromBeginning: Boolean
) -> Unit

/**
 * Correspond to [DownloadListener.fetchProgress]
 */
typealias onFetchProgress = (
        task: DownloadTask, currentProgress: Int, currentSize: Long, contentLength: Long, speed: Long
) -> Unit


/**
 * Correspond to [DownloadListener.taskEnd]
 */
typealias onTaskEnd = (task: DownloadTask, cause: EndCause, realCause: Throwable?) -> Unit

/**
 * Correspond to [DownloadListener.taskEnd]
 */
typealias onProgressSpeed = (task: DownloadTask, speedCalculator: SpeedCalculator) -> Unit

/**
 * A concise way to create a [DownloadListener], only the [DownloadListener.taskEnd] is necessary.
 */
fun createListener(
        onTaskStart: onTaskStart? = null,
        onConnectTrialStart: onConnectTrialStart? = null,
        onFetchStart: onFetchStart? = null,
        onFetchProgress: onFetchProgress? = null,
        onTaskEnd: onTaskEnd
): DownloadListener {
    return object : DownloadListener {

        override fun taskStart(task: DownloadTask) {
            onTaskStart?.invoke(task)
        }

        override fun connectTrialStart(task: DownloadTask) {
            onConnectTrialStart?.invoke(task)
        }


        override fun fetchStart(task: DownloadTask, isFromBeginning: Boolean) {
            onFetchStart?.invoke(task, isFromBeginning)
        }

        override fun fetchProgress(task: DownloadTask, currentProgress: Int, currentSize: Long, contentLength: Long, speed: Long) {
            onFetchProgress?.invoke(task, currentProgress, currentSize, contentLength, speed)
        }

        override fun taskEnd(task: DownloadTask, cause: EndCause,
                             realCause: Throwable?) {
            onTaskEnd?.invoke(task, cause, realCause)

        }


    }
}

fun setListener(task: DownloadTask,
                onTaskStart: onTaskStart? = null,
                onConnectTrialStart: onConnectTrialStart? = null,
                onFetchStart: onFetchStart? = null,
                onFetchProgress: onFetchProgress? = null,
                onTaskEnd: onTaskEnd,
                onProgressSpeed: onProgressSpeed?

) {
    task.downloadListener = object : DownloadListener {

        override fun taskStart(task: DownloadTask) {
            onTaskStart?.invoke(task)
        }

        override fun connectTrialStart(task: DownloadTask) {
            onConnectTrialStart?.invoke(task)
        }


        override fun fetchStart(task: DownloadTask, isFromBeginning: Boolean) {
            onFetchStart?.invoke(task, isFromBeginning)
        }

        override fun fetchProgress(task: DownloadTask, currentProgress: Int, currentSize: Long, contentLength: Long, speed: Long) {
            onFetchProgress?.invoke(task, currentProgress, currentSize, contentLength, speed)
        }

        override fun taskEnd(task: DownloadTask, cause: EndCause,
                             realCause: Throwable?) {
            onTaskEnd?.invoke(task, cause, realCause)

        }
    }
    task.speedListener = object : SpeedListener {
        override fun onProgress(task: DownloadTask, speedCalculator: SpeedCalculator) {
            onProgressSpeed?.invoke(task,speedCalculator)
        }
    }
}


