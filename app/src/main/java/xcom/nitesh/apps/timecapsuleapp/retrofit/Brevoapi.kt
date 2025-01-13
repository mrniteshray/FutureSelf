package xcom.nitesh.apps.timecapsuleapp.retrofit

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

data class EmailRequest(
    val sender: Sender,
    val to: List<Recipient>,
    val subject: String,
    val textContent: String
)

data class Sender(val email: String, val name: String)
data class Recipient(val email: String, val name: String)

interface Brevoapi {
    @Headers("Content-Type: application/json")
    @POST("v3/smtp/email")
    fun sendEmail(@Body emailRequest: EmailRequest): Call<Void>
}
