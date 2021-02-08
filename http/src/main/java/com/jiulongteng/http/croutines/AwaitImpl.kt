package com.jiulongteng.http.croutines

import com.jiulongteng.http.request.IRequest
import com.jiulongteng.http.util.LogUtil
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

@Suppress("BlockingMethodInNonBlockingContext")
internal class AwaitImpl<T>(
        private val request: IRequest<T>
) : IAwait<T> {

    override suspend fun await(): T {

        return try {
            LogUtil.logThread("AwaitImpl.await")
            request.execute()
        } catch (t: Throwable) {
            throw t
        }
    }




}