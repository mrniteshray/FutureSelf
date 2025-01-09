package xcom.nitesh.apps.timecapsuleapp.ui.addmessage

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.datepicker.MaterialDatePicker
import xcom.nitesh.apps.timecapsuleapp.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AddMsgActivity : AppCompatActivity() {

    private val dateFormatter = SimpleDateFormat("dd-mm-yyyy", Locale.getDefault())

    private lateinit var selectedDateTextView: TextView
    private var selectedFutureDate: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_msg)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val futureDateButton : Button = findViewById(R.id.futureDateButton)
        selectedDateTextView = findViewById(R.id.selectedDateTextView)

        futureDateButton.setOnClickListener {
            showMaterialDatePicker()
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
            selectedDateTextView.text = "$selectedFutureDate"
        }

        datePicker.show(supportFragmentManager, "FutureDatePicker")
    }
}