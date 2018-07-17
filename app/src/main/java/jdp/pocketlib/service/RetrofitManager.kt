/*
 * jamesdeperio/PocketLib is licensed under the
 *
 * Apache License 2.0
 * A permissive license whose main conditions require preservation of copyright and license notices. Contributors provide an express grant of patent rights. Licensed works, modifications, and larger works may be distributed under different terms and without source code.
 */

package jdp.pocketlib.service

import android.content.Context
import okhttp3.Cache
import okhttp3.Credentials
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Created by jamesdeperio on 7/5/2017
 *  jamesdeperio.github.com.codepocket.service
 */
abstract class RetrofitManager(private val context: Context) : RetrofitCycle {
    private var retrofit: Retrofit? = null
    override fun releaseMode(cache: Cache?) {
        val okHttpClient = OkHttpClient.Builder()
                .cache(cache)
                .writeTimeout(initWriteTimeOut(), TimeUnit.SECONDS)
                .connectTimeout(initConnectTimeOut(), TimeUnit.SECONDS)
                .readTimeout(initReadTimeOut(), TimeUnit.SECONDS)
                .addInterceptor (ConnectivityInterceptor(context,noInternetConnectionHandler()))
                .build()
        retrofit = Retrofit.Builder()
                .baseUrl(initBaseURL())
                .client(okHttpClient)
                .addConverterFactory(initConverterFactory())
                .addCallAdapterFactory(initRxAdapterFactory())
                .build()
    }

    override fun debugMode(cache: Cache?) {
        val logging = HttpLoggingInterceptor()
        logging.level = HttpLoggingInterceptor.Level.BODY
        val okHttpClient = OkHttpClient.Builder()
                .cache(cache)
                .writeTimeout(initWriteTimeOut(), TimeUnit.SECONDS)
                .connectTimeout(initConnectTimeOut(), TimeUnit.SECONDS)
                .readTimeout(initReadTimeOut(), TimeUnit.SECONDS)
                .addInterceptor(logging)
                .addInterceptor (ConnectivityInterceptor(context,noInternetConnectionHandler()))
                .build()
        retrofit = Retrofit.Builder()
                .baseUrl(initBaseURL())
                .client(okHttpClient)
                .addConverterFactory(initConverterFactory())
                .addCallAdapterFactory(initRxAdapterFactory())
                .build()
    }

    override fun create(service: Class<*>): Any {
        if (retrofit == null) {
            if (initCacheSize() != 0) {
                val cacheSize = initCacheSize() * 1024 * 1024
                val cache = Cache(context.cacheDir, cacheSize.toLong())
                if (isDebugMode()) debugMode(cache)
                else releaseMode(cache)
            } else {
                if (isDebugMode()) debugMode(null)
                else releaseMode(null)
            }
        }
        return retrofit!!.create(service)
    }

    override fun create(service: Class<*>, username: String, password: String): Any {
        val authToken = Credentials.basic(username, password)
        val interceptor = AuthenticationInterceptor(authToken)
        if (initCacheSize() != 0) {
            val cacheSize = initCacheSize() * 1024 * 1024
            val cache = Cache(context.cacheDir, cacheSize.toLong())
            val logging = HttpLoggingInterceptor()
            logging.level = HttpLoggingInterceptor.Level.BODY
            val okHttpClient = OkHttpClient.Builder()
                    .cache(cache)
                    .writeTimeout(initWriteTimeOut(), TimeUnit.SECONDS)
                    .connectTimeout(initConnectTimeOut(), TimeUnit.SECONDS)
                    .readTimeout(initReadTimeOut(), TimeUnit.SECONDS)
                    .addInterceptor(logging)
                    .addInterceptor(interceptor)
                    .addInterceptor (ConnectivityInterceptor(context,noInternetConnectionHandler()))
                    .build()
            retrofit = Retrofit.Builder()
                    .baseUrl(initBaseURL())
                    .client(okHttpClient)
                    .addConverterFactory(initConverterFactory())
                    .addCallAdapterFactory(initRxAdapterFactory())
                    .build()
        } else {
            val logging = HttpLoggingInterceptor()
            logging.level = HttpLoggingInterceptor.Level.BODY
            val okHttpClient = OkHttpClient.Builder()
                    .writeTimeout(initWriteTimeOut(), TimeUnit.SECONDS)
                    .connectTimeout(initConnectTimeOut(), TimeUnit.SECONDS)
                    .readTimeout(initReadTimeOut(), TimeUnit.SECONDS)
                    .addInterceptor(logging)
                    .addInterceptor(interceptor)
                    .addInterceptor (ConnectivityInterceptor(context,noInternetConnectionHandler()))
                    .build()
            retrofit = Retrofit.Builder()
                    .baseUrl(initBaseURL())
                    .client(okHttpClient)
                    .addConverterFactory(initConverterFactory())
                    .addCallAdapterFactory(initRxAdapterFactory())
                    .build()
        }
        return retrofit!!.create(service)
    }
}