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
import xcom.nitesh.apps.timecapsuleapp.ui.SignInActivity
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

        if(auth.currentUser!=null){
        }else{
            Toast.makeText(this, "Please SignIn First", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, SignInActivity::class.java))
            finish()
        }

        binding.messagetv.movementMethod = android.text.method.ScrollingMovementMethod()

        val title = intent.getStringExtra("message")
        fetchmessagebasedontitle(title)

        binding.btnLogout.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, SignInActivity::class.java))
            finish()
        }
        binding.btnDelete.setOnClickListener {
            deleteMsg(title)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun deleteMsg(title: String?) {
        val alertDialog = android.app.AlertDialog.Builder(this)
        alertDialog.setTitle("Delete Message")
        alertDialog.setMessage("Are you sure you want to delete this message?")
        alertDialog.setPositiveButton("Yes") { _, _ ->
            firestore.collection("Messages")
                .document(auth.currentUser?.uid.toString())
                .collection("capsule")
                .whereEqualTo("title", title)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    if (!querySnapshot.isEmpty) {
                        val document = querySnapshot.documents[0]
                        document.reference.delete()
                            .addOnSuccessListener {
                                Toast.makeText(this, "Message deleted successfully", Toast.LENGTH_SHORT).show()
                                startActivity(Intent(this, MainActivity::class.java))
                                finish()
                            }
                            .addOnFailureListener { exception ->
                                Toast.makeText(this, "Error: ${exception.message}", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        Toast.makeText(this, "No message found with the title: $title", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Error: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        }
        alertDialog.setNegativeButton("No") { dialog, _ ->
            dialog.dismiss()
        }
        alertDialog.show()

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
                    binding.progressBar.visibility = View.GONE
                    binding.title.visibility = View.VISIBLE
                    binding.messagetv.visibility = View.VISIBLE
                    binding.textView5.visibility = View.VISIBLE
                    binding.textView6.visibility = View.VISIBLE
                    binding.title.text = message[0].title
                    binding.messagetv.text = message[0].content
                    binding.btnDelete.visibility = View.VISIBLE
                } else {
                    binding.progressBar.visibility = View.GONE
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                    Toast.makeText(this, "No Message Found", Toast.LENGTH_SHORT).show()
                }
            }
    }
}