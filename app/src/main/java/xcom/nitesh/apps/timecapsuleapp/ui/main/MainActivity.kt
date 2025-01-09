package xcom.nitesh.apps.timecapsuleapp.ui.main

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import xcom.nitesh.apps.timecapsuleapp.Model.Capsule
import xcom.nitesh.apps.timecapsuleapp.R
import xcom.nitesh.apps.timecapsuleapp.ui.addmessage.AddMsgActivity

class MainActivity : AppCompatActivity() {

    lateinit var capsuleAdapter: CapsuleAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val dummyCapsules = listOf(
            Capsule(
                "Added a time capsule for the trip to Paris.",
                "Date: Jan 2023, Shared with friends."
            ),
            Capsule(
                "Saved a memory of the family dinner.",
                "Date: Feb 2023, Tagged: #Family"
            ),
            Capsule(
                "Added a time capsule for the trip to Paris.",
                "Date: Jan 2023, Shared with friends."
            ),
            Capsule(
                "Saved a memory of the family dinner.",
                "Date: Feb 2023, Tagged: #Family"
            ),
            Capsule(
                "Added a time capsule for the trip to Paris.",
                "Date: Jan 2023, Shared with friends."
            ),
            Capsule(
                "Saved a memory of the family dinner.",
                "Date: Feb 2023, Tagged: #Family"
            ),
            Capsule(
                "Added a time capsule for the trip to Paris.",
                "Date: Jan 2023, Shared with friends."
            ),
            Capsule(
                "Saved a memory of the family dinner.",
                "Date: Feb 2023, Tagged: #Family"
            ),
        )
        val rcv = findViewById<RecyclerView>(R.id.rec)

        rcv.layoutManager = LinearLayoutManager(this)
        capsuleAdapter = CapsuleAdapter(dummyCapsules)
        rcv.adapter = capsuleAdapter

        val fab = findViewById<LinearLayout>(R.id.btn_fab)
        fab.setOnClickListener {
            val intent = Intent(this, AddMsgActivity::class.java)
            startActivity(intent)
        }

    }
}