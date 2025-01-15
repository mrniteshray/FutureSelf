package xcom.nitesh.apps.timecapsuleapp.retrofit

import android.content.Context
import android.widget.Toast
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import xcom.nitesh.apps.timecapsuleapp.utils.CONSTANTS.Companion.getApiKey
object RetrofitInstance {
    private const val BASE_URL = "https://api.brevo.com/"

    fun apiservice(context: Context): Brevoapi {
        val api: Brevoapi by lazy {
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
            val client = OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .addInterceptor { chain ->
                    val request = chain.request().newBuilder()
                        .addHeader("api-key",getApiKey(context))
                        .build()
                    chain.proceed(request)
                }
                .build()

            Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(Brevoapi::class.java)
        }
        return api
    }
}
