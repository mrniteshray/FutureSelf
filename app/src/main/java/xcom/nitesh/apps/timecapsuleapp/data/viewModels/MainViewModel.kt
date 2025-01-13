package xcom.nitesh.apps.timecapsuleapp.data.viewModels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import xcom.nitesh.apps.timecapsuleapp.data.Model.CapsuleData
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject


class MainViewModel @Inject constructor() : ViewModel() {
    private val _messagelist = MutableLiveData<List<CapsuleData>>()
    val messages: LiveData<List<CapsuleData>> get() = _messagelist
    private val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    fun fetchData() {
        val userId = auth.currentUser?.uid ?: return

        firestore.collection("Messages")
            .document(userId)
            .collection("capsule")
            .get()
            .addOnSuccessListener { querySnapshot ->
                val updatedMessages = mutableListOf<CapsuleData>()
                val currentDate = dateFormatter.format(Date())

                for (document in querySnapshot.documents) {
                    val capsule = document.toObject(CapsuleData::class.java) ?: continue
                    val unlockDate = capsule.unlockDate ?: continue

                    if (!capsule.isUnlocked && unlockDate <= currentDate.toString()) {
                        firestore.collection("Messages")
                            .document(userId)
                            .collection("capsule")
                            .document(document.id)
                            .update("isUnlocked", true)
                            .addOnSuccessListener {
                                Log.d("MainViewModel", "Capsule unlocked: ${document.id}")
                            }
                            .addOnFailureListener {
                                Log.e("MainViewModel", "Error unlocking capsule: ${it.message}")
                            }

                        // Update local data as unlocked
                        capsule.isUnlocked = true
                    }

                    updatedMessages.add(capsule)
                }

                _messagelist.value = updatedMessages
            }
            .addOnFailureListener {
                Log.e("MainViewModel", "Error fetching data: ${it.message}")
            }
    }
}

//class MainViewModel() : ViewModel() {
//    private val _messagelist = MutableLiveData<List<CapsuleData>>()
//    val messages: LiveData<List<CapsuleData>> get() = _messagelist
//
//    private val firestore = FirebaseFirestore.getInstance()
//
//    fun fetchData() {
//        firestore.collection("Messages")
//            .document(FirebaseAuth.getInstance().currentUser?.uid.toString())
//            .collection("capsule")
//            .get()
//            .addOnSuccessListener {
//                val messages = it.toObjects(CapsuleData::class.java)
//                _messagelist.value = messages
//            }
//    }
//}