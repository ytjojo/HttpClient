package com.jiulongteng.http.croutines

import com.jiulongteng.http.entities.IResult
import com.jiulongteng.http.exception.ExceptionHandle
import com.jiulongteng.http.request.HttpRequest
import com.jiulongteng.http.request.IRequest
import com.jiulongteng.http.rx.SimpleObserver
import com.jiulongteng.http.util.LogUtil
import io.reactivex.rxjava3.disposables.Disposable
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.intrinsics.COROUTINE_SUSPENDED
import kotlin.coroutines.intrinsics.intercepted
import kotlin.coroutines.intrinsics.suspendCoroutineUninterceptedOrReturn

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

suspend inline fun <reified T : Any> IRequest<Any>.castAsync(
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

suspend inline fun <reified T : Any> IRequest<Any>.castAwait(): T {
    val type = T::class.java;
    return asType<T>(type).await()
}

inline fun <reified T : Any> IRequest<Any>.toAwaitAsType(): IAwait<T> {
    val type = T::class.java;
    return this.asType<T>(type).toAwait()
}

fun <T> IRequest<T>.toAwait(): IAwait<T> = AwaitImpl<T>(this)


suspend fun <T> IRequest<T>.rxAwait(): T {

    return suspendCancellableCoroutine { continuation ->
        var disposable: Disposable? = null
        continuation.invokeOnCancellation {
            if (disposable != null && !disposable!!.isDisposed) {
                disposable?.dispose()
            }
        }
        try {
            subscribe(object : SimpleObserver<T>() {
                override fun onSubscribe(d: Disposable) {
                    super.onSubscribe(d)
                    disposable = d
                }

                override fun onNext(t: T) {
                    continuation.resume(t)
                }

                override fun onError(e: Throwable) {
                    continuation.resumeWithException(e)
                }
            })

        } catch (t: Throwable) {
            continuation.resumeWithException(t)
        }
    }
}

