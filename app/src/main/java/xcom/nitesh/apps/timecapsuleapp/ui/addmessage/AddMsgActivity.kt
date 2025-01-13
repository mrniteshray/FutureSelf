package xcom.nitesh.apps.timecapsuleapp.ui.addmessage

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import java.time.LocalDate
import java.time.ZoneId
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Observer
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import xcom.nitesh.apps.timecapsuleapp.R
import xcom.nitesh.apps.timecapsuleapp.databinding.ActivityAddMsgBinding
import xcom.nitesh.apps.timecapsuleapp.ui.SignInActivity
import xcom.nitesh.apps.timecapsuleapp.ui.main.MainActivity
import xcom.nitesh.apps.timecapsuleapp.utils.NotificationReceiver
import xcom.nitesh.apps.timecapsuleapp.data.viewModels.AddMsgViewModel
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class AddMsgActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddMsgBinding
    private val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) // Correct format
    private val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())
    private var selectedFutureDate: String? = null
    private var selectedFutureTime: String? = null

    @Inject
    lateinit var addMsgViewModel: AddMsgViewModel

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

        binding.btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            Intent(this, SignInActivity::class.java).also {
                startActivity(it)
            }
            finish()
        }

        checkAndRequestExactAlarmPermission()
        createNotificationChannel()
        checkAndRequestNotificationPermission()
        binding.futureDateButton.setOnClickListener {
            showMaterialDatePicker()
        }

        addMsgViewModel.uiState.observe(this, Observer { value ->
            if(value){
                Toast.makeText(this, "Message Added Successfully", Toast.LENGTH_SHORT).show()
                Intent(this, MainActivity::class.java).also {
                    startActivity(it)
                }
                finish()
            }
        })

        binding.Savebtn.setOnClickListener {
            binding.progressBar.visibility = View.VISIBLE
            val title = binding.title.text.toString()
            val message = binding.messagetv.text.toString()
            if(title.isEmpty() || message.isEmpty() || selectedFutureDate == null || selectedFutureTime == null){
                binding.progressBar.visibility = View.GONE
                showValidationErrors(title, message)
            }
            else{
                val parsedFutureDate = parseSelectedDateTime(selectedFutureDate, selectedFutureTime)
                if (parsedFutureDate != null) {
                    scheduleNotification(this, "✨You've got mail from yourself!✨", title, parsedFutureDate)
                } else {
                    Toast.makeText(this, "Invalid Date. Please select a valid date.", Toast.LENGTH_SHORT).show()
                }
                addMsgViewModel.saveToFirebase(title, message, selectedFutureDate!!,selectedFutureTime!!)
            }
        }

    }

    private fun showValidationErrors(title: String, message: String) {
        if (title.isEmpty()) Toast.makeText(this, "Please Enter Title", Toast.LENGTH_SHORT).show()
        if (message.isEmpty()) Toast.makeText(this, "Please Enter Message", Toast.LENGTH_SHORT).show()
        if (selectedFutureDate == null) Toast.makeText(this, "Please Select Future Date", Toast.LENGTH_SHORT).show()
        if (selectedFutureTime == null) Toast.makeText(this, "Please Select Future Time", Toast.LENGTH_SHORT).show()
    }


    private fun scheduleNotification(context: Context, title: String, message: String, futureDateTime: LocalDateTime) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("title", title)
            putExtra("message", message)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            futureDateTime.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerTimeMillis = futureDateTime
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerTimeMillis,
            pendingIntent
        )
    }

    override fun onBackPressed() {
        super.onBackPressed()
        Intent(this, MainActivity::class.java).also {
            startActivity(it)
        }
        finish()
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

    private fun checkAndRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13+
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // Request the permission
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    NOTIFICATION_PERMISSION_REQUEST_CODE
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Notification permission granted!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Notification permission denied. Notifications won't work.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        private const val NOTIFICATION_PERMISSION_REQUEST_CODE = 1
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Capsule Channel"
            val descriptionText = "Channel for capsule notifications"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("capsule_channel", name, importance).apply {
                description = descriptionText
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun checkAndRequestExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) { // Android 12+
            val alarmManager = getSystemService(AlarmManager::class.java)
            if (!alarmManager.canScheduleExactAlarms()) {
                // Notify the user or direct them to settings
                AlertDialog.Builder(this)
                    .setTitle("Enable Exact Alarm Permission")
                    .setMessage("This app requires exact alarm permission to schedule future notifications. Please enable it in the app settings.")
                    .setPositiveButton("Open Settings") { _, _ ->
                        val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                            data = Uri.parse("package:$packageName")
                        }
                        startActivity(intent)
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        }
    }
}