package xcom.nitesh.apps.timecapsuleapp.ui.display

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import xcom.nitesh.apps.timecapsuleapp.Model.CapsuleData
import xcom.nitesh.apps.timecapsuleapp.R
import xcom.nitesh.apps.timecapsuleapp.databinding.ActivityDetailBinding
import xcom.nitesh.apps.timecapsuleapp.ui.main.MainActivity

class DetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBinding
    private var auth = FirebaseAuth.getInstance()
    private var firestore = FirebaseFirestore.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val title = intent.getStringExtra("message")
        fetchmessagebasedontitle(title)
    }

    private fun fetchmessagebasedontitle(title: String?) {
        firestore.collection("Messages")
            .document(auth.currentUser?.uid.toString())
            .collection("capsule")
            .whereEqualTo("title", title)
            .get()
            .addOnSuccessListener {
                if (!it.isEmpty) {
                    val message = it.toObjects(CapsuleData::class.java)
                    binding.title.text = message[0].title
                    binding.messagetv.text = message[0].content
                    binding.progressBar.visibility = View.GONE
                } else {
                    Toast.makeText(this, "No Message Found", Toast.LENGTH_SHORT).show()
                }
            }
    }
}