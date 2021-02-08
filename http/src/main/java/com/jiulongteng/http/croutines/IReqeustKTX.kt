package com.jiulongteng.http.croutines

import com.jiulongteng.http.entities.IResult
import com.jiulongteng.http.exception.ExceptionHandle
import com.jiulongteng.http.request.HttpRequest
import com.jiulongteng.http.request.IRequest
import com.jiulongteng.http.util.LogUtil
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

suspend fun <T> IRequest<T>.execute(): T {
    return try {
        val response = retrofit.create(SuspendService::class.java).get(mergedHeaders, relativeUrl)
        val request = this
        val result = withContext<Any>(Dispatchers.IO) {
            HttpRequest.convertToIResult(request, response)
        }
        LogUtil.logThread("execute return result")
        if (isIResultResponse || (result !is IResult<*>)) {
            result as T
        } else {
            result?.data as T
        }
    } catch (t: Throwable) {
        throw ExceptionHandle.handleException(t)
    }
}

suspend fun <T> IRequest<T>.await(): T = AwaitImpl<T>(this).await()

suspend fun <T> IRequest<T>.async(
        scope: CoroutineScope,
        context: CoroutineContext = SupervisorJob(),
        start: CoroutineStart = CoroutineStart.DEFAULT
): Deferred<T> = scope.async(context, start) {
    await()
}

suspend inline fun <reified T : Any> IRequest<Any>.asyncAs(
        scope: CoroutineScope,
        context: CoroutineContext = SupervisorJob(),
        start: CoroutineStart = CoroutineStart.DEFAULT
): Deferred<T> {
    val type = T::class.java;
    val request = this.asType<T>(type)
    return scope.async(context, start) {
        request.await()
    }
}


inline fun <T : Any> IRequest<Any>.cast(): IRequest<T> {
    return this as IRequest<T>;
}

suspend inline fun <reified T : Any> IRequest<Any>.awaitAs(): T {
    val type = T::class.java;
    return asType<T>(type).await()
}

inline fun <reified T : Any> IRequest<Any>.toAwaitAsType(): IAwait<T> {
    val type = T::class.java;
    return this.asType<T>(type).toAwait()
}

fun <T> IRequest<T>.toAwait(): IAwait<T> = AwaitImpl<T>(this)


suspend fun <T> suspendExecute(block: () -> T): T {
    return suspendCancellableCoroutine { continuation ->
        try {
            continuation.resume(block.invoke())

        } catch (t: Throwable) {

            continuation.resumeWithException(t)
        }
    }

}