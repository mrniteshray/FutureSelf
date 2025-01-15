package xcom.nitesh.apps.timecapsuleapp.utils

import android.content.Context
import org.json.JSONObject
import xcom.nitesh.apps.timecapsuleapp.R

class CONSTANTS() {

    companion object{
        fun getApiKey(context: Context): String {
            val inputStream = context.resources.openRawResource(R.raw.api)
            val json = inputStream.bufferedReader().use { it.readText() }
            val jsonObject = JSONObject(json)
            return jsonObject.getString("Brevo_API")
        }
    }
}