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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import xcom.nitesh.apps.timecapsuleapp.Model.CapsuleData
import xcom.nitesh.apps.timecapsuleapp.R
import xcom.nitesh.apps.timecapsuleapp.databinding.ActivityAddMsgBinding
import xcom.nitesh.apps.timecapsuleapp.ui.main.MainActivity
import xcom.nitesh.apps.timecapsuleapp.utils.NotificationWorker
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

class AddMsgActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddMsgBinding
    private val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) // Correct format
    private var selectedFutureDate: String? = null

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
            if(title.isEmpty() || message.isEmpty() || selectedFutureDate == null){
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
            }
            else{
                val parsedFutureDate = parseSelectedDate(selectedFutureDate ?: "")
                if (parsedFutureDate != null) {
                    scheduleNotification(this, "✨You've got mail from yourself!✨", title, parsedFutureDate)
                } else {
                    Toast.makeText(this, "Invalid Date. Please select a valid date.", Toast.LENGTH_SHORT).show()
                }
                saveToFirebase(title, message, selectedFutureDate!!)
            }
        }


        val title = intent.getStringExtra("title")
        binding.title.setText(title)

    }

    private fun saveToFirebase(title: String, message: String, selectedFutureDate: String) {
        val currentuser = auth.currentUser?.uid

        val newMessage = CapsuleData(
            title = title,
            content = message,
            createdDate = dateFormatter.format(Date()),
            unlockDate = selectedFutureDate,
            isUnlocked = false
        )

        DbRef.collection("Messages")
            .document(currentuser.toString())
            .collection("capsule")
            .add(newMessage)
            .addOnSuccessListener {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this, "Message Added Successfully", Toast.LENGTH_SHORT).show()
                Intent(this, MainActivity::class.java).also {
                    startActivity(it)
                }
            }


    }

    fun scheduleNotification(
        context: Context,
        title: String,
        message: String,
        futureDate: LocalDate
    ) {
        val triggerTime = futureDate.atTime(0, 0) // 8:00 AM
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
        val delay = triggerTime - System.currentTimeMillis()

        // Prepare data to pass to Worker
        val data = workDataOf(
            "title" to title,
            "message" to message
        )

        // Schedule WorkManager task
        val notificationWorkRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
            .setInputData(data)
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(context).enqueue(notificationWorkRequest)
    }


    fun parseSelectedDate(selectedDate: String): LocalDate? {
        return try {
            val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.getDefault())
            LocalDate.parse(selectedDate, formatter)
        } catch (e: Exception) {
            Log.d("AddMsgActivity", "Error parsing date: $e")
            Toast.makeText(this,"Error parsing date: ${e.message}",Toast.LENGTH_LONG).show()
            null
        }
    }

    private fun showMaterialDatePicker() {
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select Future Date")
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
            .build()

        datePicker.addOnPositiveButtonClickListener { timestamp ->
            val date = Date(timestamp)
            selectedFutureDate = dateFormatter.format(date)
            binding.selectedDateTextView.text = selectedFutureDate

            if (selectedFutureDate != null && validateFutureDate(selectedFutureDate!!)) {
                Toast.makeText(this, "Selected Date: $selectedFutureDate", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Please select a valid future date!", Toast.LENGTH_SHORT).show()
                selectedFutureDate = null
                binding.selectedDateTextView.text = ""
            }
        }

        datePicker.show(supportFragmentManager, "FutureDatePicker")
    }

    fun validateFutureDate(selectedDate: String): Boolean {
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.getDefault())
        val selectedLocalDate = LocalDate.parse(selectedDate, formatter)
        return !selectedLocalDate.isBefore(LocalDate.now()) // Ensures date is not in the past
    }

}