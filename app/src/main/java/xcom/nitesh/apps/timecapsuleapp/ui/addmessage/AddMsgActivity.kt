package xcom.nitesh.apps.timecapsuleapp.ui.addmessage

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.content.Context
import android.util.Log
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import java.time.LocalDate
import java.time.ZoneId
import java.util.concurrent.TimeUnit
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import xcom.nitesh.apps.timecapsuleapp.Model.CapsuleData
import xcom.nitesh.apps.timecapsuleapp.R
import xcom.nitesh.apps.timecapsuleapp.databinding.ActivityAddMsgBinding
import xcom.nitesh.apps.timecapsuleapp.ui.main.MainActivity
import xcom.nitesh.apps.timecapsuleapp.utils.NotificationWorker
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AddMsgActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddMsgBinding
    private val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) // Correct format
    private val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())
    private var selectedFutureDate: String? = null
    private var selectedFutureTime: String? = null

    private var auth = FirebaseAuth.getInstance()
    private var DbRef = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAddMsgBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        binding.futureDateButton.setOnClickListener {
            showMaterialDatePicker()
        }

        binding.Savebtn.setOnClickListener {
            binding.progressBar.visibility = View.VISIBLE
            val title = binding.title.text.toString()
            val message = binding.messagetv.text.toString()
            if(title.isEmpty() || message.isEmpty() || selectedFutureDate == null || selectedFutureTime == null){
                binding.progressBar.visibility = View.GONE
                if(title.isEmpty()){
                    Toast.makeText(this, "Please Enter Title", Toast.LENGTH_SHORT).show()
                }
                if(message.isEmpty()) {
                    Toast.makeText(this, "Please Enter Message", Toast.LENGTH_SHORT).show()
                }
                if(selectedFutureDate == null){
                    Toast.makeText(this, "Please Select Future Date", Toast.LENGTH_SHORT).show()
                }
                if(selectedFutureTime == null){
                    Toast.makeText(this, "Please Select Future Time", Toast.LENGTH_SHORT).show()
                }
            }
            else{
                val parsedFutureDate = parseSelectedDateTime(selectedFutureDate, selectedFutureTime)
                if (parsedFutureDate != null) {
                    scheduleNotification(this, "✨You've got mail from yourself!✨", title, parsedFutureDate)
                } else {
                    Toast.makeText(this, "Invalid Date. Please select a valid date.", Toast.LENGTH_SHORT).show()
                }
                saveToFirebase(title, message, selectedFutureDate!!,selectedFutureTime!!)
            }
        }


        val title = intent.getStringExtra("title")
        binding.title.setText(title)

    }

    private fun saveToFirebase(title: String, message: String, selectedFutureDate: String,selectedFutureTime: String) {
        val currentuser = auth.currentUser?.uid

        val newMessage = CapsuleData(
            title = title,
            content = message,
            createdDate = dateFormatter.format(Date()),
            unlockDate = selectedFutureDate,
            unlockTime = selectedFutureTime,
            isUnlocked = false
        )

        DbRef.collection("Messages")
            .document(currentuser.toString())
            .collection("capsule")
            .add(newMessage)
            .addOnSuccessListener {
                binding.progressBar.visibility = View.GONE
                Intent(this, MainActivity::class.java).also {
                    startActivity(it)
                }
            }
    }

    fun scheduleNotification(
        context: Context,
        title: String,
        message: String,
        futureDate: LocalDateTime
    ) {
        val triggerTime = futureDate
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
        val delay = triggerTime - System.currentTimeMillis()
        val data = workDataOf(
            "title" to title,
            "message" to message
        )

        val notificationWorkRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
            .setInputData(data)
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(context).enqueue(notificationWorkRequest)
    }

    private fun showMaterialDatePicker() {
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select Future Date")
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
            .build()

        datePicker.addOnPositiveButtonClickListener { timestamp ->
            val date = Date(timestamp)
            selectedFutureDate = dateFormatter.format(date)

            showMaterialTimePicker() // Open Time Picker after selecting the date
        }

        datePicker.show(supportFragmentManager, "FutureDatePicker")
    }

    private fun showMaterialTimePicker() {
        val timePicker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_12H) // 12-hour format with AM/PM
            .setTitleText("Select Future Time")
            .build()

        timePicker.addOnPositiveButtonClickListener {
            val hour = timePicker.hour
            val minute = timePicker.minute

            val selectedTime = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
            }
            selectedFutureTime = timeFormatter.format(selectedTime.time)
            //Am or Pm
            val amPm = if (hour < 12) "AM" else "PM"
            binding.selectedDateTextView.text =
                "$selectedFutureDate \n $selectedFutureTime $amPm"
        }

        timePicker.show(supportFragmentManager, "FutureTimePicker")
    }

    fun parseSelectedDateTime(date: String?, time: String?): LocalDateTime? {
        return try {
            val datePart = LocalDate.parse(date, DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.getDefault()))
            val timePart = LocalTime.parse(time, DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault()))
            LocalDateTime.of(datePart, timePart)
        } catch (e: Exception) {
            Log.d("AddMsgActivity", "Error parsing date/time: $e")
            null
        }
    }

}