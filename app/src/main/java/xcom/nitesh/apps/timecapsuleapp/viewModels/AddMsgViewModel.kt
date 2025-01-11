package xcom.nitesh.apps.timecapsuleapp.viewModels

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import xcom.nitesh.apps.timecapsuleapp.Model.CapsuleData
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AddMsgViewModel(val context : Context) : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private val DbRef = FirebaseFirestore.getInstance()

    private val _uiState = MutableLiveData<Boolean>(false)
    val uiState: LiveData<Boolean> get() = _uiState


    fun saveToFirebase(title: String, message: String, selectedFutureDate: String,selectedFutureTime: String) {
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
                _uiState.postValue(true)
            }.addOnFailureListener{
                _uiState.postValue(false)
            }
    }

}