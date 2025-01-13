package xcom.nitesh.apps.timecapsuleapp.ui.main

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import xcom.nitesh.apps.timecapsuleapp.R
import xcom.nitesh.apps.timecapsuleapp.databinding.ActivityMainBinding
import xcom.nitesh.apps.timecapsuleapp.ui.SignInActivity
import xcom.nitesh.apps.timecapsuleapp.ui.addmessage.AddMsgActivity
import xcom.nitesh.apps.timecapsuleapp.ui.display.DetailActivity
import xcom.nitesh.apps.timecapsuleapp.data.viewModels.MainViewModel
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val requestNotificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                Toast.makeText(this, "Notification permission granted!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Notification permission denied!", Toast.LENGTH_SHORT).show()
            }
        }

    private lateinit var binding: ActivityMainBinding
    lateinit var capsuleAdapter: CapsuleAdapter

    @Inject
    lateinit var mainViewModel : MainViewModel

    private val auth = FirebaseAuth.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        checkAndRequestNotificationPermission()


        binding.rec.layoutManager = LinearLayoutManager(this)

        mainViewModel.fetchData()

        mainViewModel.messages.observe(this, Observer {
            if (it.isEmpty()){
                binding.textView7.visibility = View.VISIBLE
                binding.progressBar2.visibility = View.GONE
            }
            capsuleAdapter = CapsuleAdapter(it){ message->
                Intent(this, DetailActivity::class.java).also {
                    it.putExtra("message",message.title)
                    it.putExtra("content",message.content)
                    startActivity(it)
                    finish()
                }
            }
            binding.rec.adapter = capsuleAdapter
            binding.progressBar2.visibility = View.GONE
            capsuleAdapter.notifyDataSetChanged()
        })

        binding.btnFab.setOnClickListener {
            Intent(this, AddMsgActivity::class.java).also {
                startActivity(it)
                finish()
            }
        }

        binding.btnLogout.setOnClickListener {
            auth.signOut()
            Intent(this, SignInActivity::class.java).also {
                startActivity(it)
            }
            finish()
        }
    }
        fun checkAndRequestNotificationPermission() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                } else {
                    // Request the permission
                    requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
}