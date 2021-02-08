package com.ytjojo.practice;

import androidx.annotation.NonNull
import com.google.gson.reflect.TypeToken
import com.jiulongteng.http.annotation.RawString
import com.jiulongteng.http.client.AbstractClient
import com.jiulongteng.http.client.AbstractHttpClientFactory
import com.jiulongteng.http.client.HttpClient
import com.jiulongteng.http.client.IHttpClientBuilder
import com.jiulongteng.http.converter.GsonResponseBodyConverter
import com.jiulongteng.http.croutines.*
import com.jiulongteng.http.entities.IResult
import com.jiulongteng.http.rx.SimpleObserver
import com.jiulongteng.http.util.LogUtil
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.*
import okhttp3.ResponseBody
import org.junit.Test
import retrofit2.Converter
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.HeaderMap
import retrofit2.http.Url
import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class CoroutinesTest {

    var abstractHttpClientFactory: AbstractHttpClientFactory? = null

    @NonNull
    fun init(): AbstractHttpClientFactory? {
        abstractHttpClientFactory = object : AbstractHttpClientFactory() {
            override fun getHttpCacheParent(): File? {
                return null
            }
        }
        abstractHttpClientFactory?.setHttpClientBuilder(object : IHttpClientBuilder {
            override fun createByTag(tag: Any): AbstractClient? {
                return null
            }

            override fun createByUrl(baseUrl: String): AbstractClient {
                return HttpClient(baseUrl)
            }

            override fun getDefaultClient(): AbstractClient {
                return object : HttpClient("http://api.map.baidu.com") {
                    override fun getBoundaryResultClass(): Class<out IResult<Any>> {
                        return HttpTest.SuccResult::class.java as Class<out IResult<Any>>
                    }
                };
            }
        })
        return abstractHttpClientFactory
    }


    @Test
    fun test() {
        abstractHttpClientFactory = init()
        val sevice = abstractHttpClientFactory?.getDefaultClient()
                ?.retrofit?.create(Service::class.java)
        val result = runBlocking {
            val body = sevice?.get(HashMap<String, String>(), "http://api.k780.com/?app=weather.today&weaid=1&appkey=10003&sign=b59bc3ef6191eb9f747dd4e83c99f2a4&format=json")

            val converter: Converter<ResponseBody, HttpTest.Weather>? = abstractHttpClientFactory!!.defaultClient.retrofit?.responseBodyConverter(HttpTest.Weather::class.java, emptyArray())
            if (converter is GsonResponseBodyConverter) {
                converter.setBoundaryResultClass(HttpTest.SuccResult::class.java)
            }
            converter!!.convert(body!!.body())

        }
        println((result as HttpTest.Weather).citynm)

    }


    public interface Service {
        @GET()
        suspend fun body(@Url url: String): String

        @GET()
        suspend fun bodyNullable(): String?

        @GET()
        suspend fun  getDefferred(@HeaderMap headers: Map<String, String>, @Url url: String): Deferred<Response<ResponseBody>>


        @RawString
        @GET
        suspend fun get(@HeaderMap headers: Map<String, String>, @Url url: String): Response<ResponseBody>

    }


    @Test
    public fun test1() {

        runBlocking {

            val start = System.currentTimeMillis();

            val s1 = GlobalScope.async(Dispatchers.IO) {

                suspendCancellableCoroutine<Any?> { continuation ->


                    Observable.just(1)
                            .subscribeOn(Schedulers.io())
                            .delay(1000L, TimeUnit.MILLISECONDS)
                            .subscribe(object : SimpleObserver<Int>() {
                                override fun onNext(t: Int?) {
                                    continuation.resume(t)
                                }

                                override fun onError(e: Throwable?) {
//                                    continuation.resumeWithException(e)
                                }
                            })
                }
            }
            val s2 = GlobalScope.async {

                (Dispatchers.IO) {
                    suspendCancellableCoroutine<Any?> { continuation ->
                        continuation.invokeOnCancellation {

                        }

                        Observable.just(1)
                                .subscribeOn(Schedulers.io())
                                .delay(2000L, TimeUnit.MILLISECONDS)
                                .map {
                                    throw IllegalArgumentException("sssss")
                                }
                                .subscribe({
                                    continuation.resume(it)
                                })
                    }
                }


            }

            val value1 = s1.await();
            val value2 = s2.await();

            val cost = System.currentTimeMillis() - start;
            System.out.println("$cost  $value1 $value2")


        }


    }

    @Test
    fun test2() {
        runBlocking {
            val start = System.currentTimeMillis();

            System.out.println("${Thread.currentThread().id}  ${Thread.currentThread().name}")
            val value = GlobalScope.async(Dispatchers.IO) {
                execute()
            }.await()
            System.out.println("${Thread.currentThread().id}  ${Thread.currentThread().name}  ${System.currentTimeMillis() - start}")


        }
    }

    suspend fun execute(): Int {
        return suspendCancellableCoroutine<Int> { contnuation ->
            System.out.println("${Thread.currentThread().id}  ${Thread.currentThread().name}")
            Thread.sleep(2000)

            contnuation.resume(2)
        }

    }

    suspend fun <T> suspendExecute(block: () -> T): T {
        return suspendCancellableCoroutine<T> { continuation ->

            try {
                continuation.resume(block.invoke(), {})

            } catch (t: Throwable) {

                continuation.resumeWithException(t)
            }
        }

    }

    @Test
    fun test3() {


    }

    @Test
    fun test4() {
        abstractHttpClientFactory = init()
        LogUtil.logThread("start")
        runBlocking {
            val s1 = abstractHttpClientFactory?.defaultClient?.get("http://api.k780.com/?app=weather.today&weaid=1&appkey=10003&sign=b59bc3ef6191eb9f747dd4e83c99f2a4&format=json")
                    ?.asType(object : TypeToken<HttpTest.Weather>() {})
                    ?.setBoundaryResultClass(HttpTest.SuccResult::class.java)
                    ?.toAwait()
            LogUtil.logThread("before await")

            try {
                val result = s1?.await()
                System.out.println("${Thread.currentThread().id}  ${Thread.currentThread().name}   ${result?.citynm}")
                result
            }catch (e :Throwable){
                System.out.println(e.message)
            }

        }


    }

    @Test
    fun test5() {
        abstractHttpClientFactory = init()
        System.out.println("${Thread.currentThread().id}  test4 ${Thread.currentThread().name}")
        runBlocking {
             val ss = abstractHttpClientFactory?.defaultClient?.get("http://api.k780.com/?app=weather.today&weaid=1&appkey=10003&sign=b59bc3ef6191eb9f747dd4e83c99f2a4&format=json")
                ?.setBoundaryResultClass(HttpTest.SuccResult::class.java)
                ?.awaitAs<HttpTest.Weather>()
            System.out.println("${Thread.currentThread().id}  ${Thread.currentThread().name} async   ${ss?.citynm}")

            try {
                val result = ss
                System.out.println("${Thread.currentThread().id}  ${Thread.currentThread().name}   ${result?.citynm}")
                result
            }catch (e :Throwable){
                System.out.println(e.message)
            }

        }


    }

    @Test
    fun test6() {
        abstractHttpClientFactory = init()
        System.out.println("${Thread.currentThread().id}  test4 ${Thread.currentThread().name}")
        runBlocking {
            val s1 = GlobalScope.async {

                val ss:HashMap<String,String>? = abstractHttpClientFactory?.defaultClient?.get("http://api.k780.com/?app=weather.today&weaid=1&appkey=10003&sign=b59bc3ef6191eb9f747dd4e83c99f2a4&format=json")
                        ?.setBoundaryResultClass(HttpTest.SuccResult::class.java)
                        ?.awaitAs<HashMap<String,String>>()
                System.out.println("${Thread.currentThread().id}  ${Thread.currentThread().name} async   ${ss?.get("citynm")}")
                ss
            }

            try {
                val result = s1.await()
                System.out.println("${Thread.currentThread().id}  ${Thread.currentThread().name}   ${result?.get("citynm")}")
                result
            }catch (e :Throwable){
                System.out.println(e.message)
            }

        }


    }

    @Test
    fun test7() {
        abstractHttpClientFactory = init()
        LogUtil.logThread("start")
        runBlocking {
            val s1 = GlobalScope.async {

                val deferred = abstractHttpClientFactory?.defaultClient?.get("http://api.k780.com/?app=weather.today&weaid=1&appkey=10003&sign=b59bc3ef6191eb9f747dd4e83c99f2a4&format=json")
                        ?.setBoundaryResultClass(HttpTest.SuccResult::class.java)
                        ?.asType(object : TypeToken<HttpTest.Weather>() {})
                        ?.async(this)
                val ss = deferred?.await()
                LogUtil.logThread("in async")
                ss
            }

            try {
                val result = s1.await()
                LogUtil.logThread("after async await  ${result?.citynm}")
                result
            }catch (e :Throwable){
                System.out.println(e.message)
            }

        }


    }

    @Test
    fun test9() {
        abstractHttpClientFactory = init()
        LogUtil.logThread("start")
        runBlocking {
            val s1 = GlobalScope.async {

                val deferred = abstractHttpClientFactory?.defaultClient?.get("http://api.k780.com/?app=weather.today&weaid=1&appkey=10003&sign=b59bc3ef6191eb9f747dd4e83c99f2a4&format=json")
                        ?.setBoundaryResultClass(HttpTest.SuccResult::class.java)
                        ?.asyncAs<HttpTest.Weather>(this)
                val ss = deferred?.await()
                ss
            }

            try {
                val result = s1.await()
                System.out.println("${Thread.currentThread().id}  ${Thread.currentThread().name}   ${result?.citynm}")
                result
            }catch (e :Throwable){
                System.out.println(e.message)
            }

        }

    }

    @Test
    fun test10() {

       val handler = CoroutineExceptionHandler{context, exception ->

        }
        abstractHttpClientFactory = init()
        LogUtil.logThread("start")
        runBlocking (handler){
            val job = SupervisorJob();
            val ss = abstractHttpClientFactory?.defaultClient?.get("http://api.k780.com/?app=weather.today&weaid=1&appkey=10003&sign=b59bc3ef6191eb9f747dd4e83c99f2a4&format=json")
                    ?.setBoundaryResultClass(HttpTest.SuccResult::class.java)
                    ?.asyncAs<HttpTest.Weather>(this,job)
            LogUtil.logThread("after async")
            job.cancel("我取消了")
            try {
                val result = ss?.await()
                LogUtil.logThread("after await")
                System.out.println("${result?.citynm}")
                result
            }catch (e :Throwable){
                System.out.println("exception "+ e.message)
            }

        }

    }



}


