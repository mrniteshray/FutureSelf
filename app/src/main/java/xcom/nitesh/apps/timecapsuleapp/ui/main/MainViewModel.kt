package xcom.nitesh.apps.timecapsuleapp.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import xcom.nitesh.apps.timecapsuleapp.Model.CapsuleData

class MainViewModel() : ViewModel() {
    private val _messagelist = MutableLiveData<List<CapsuleData>>()
    val messages: LiveData<List<CapsuleData>> get() = _messagelist

    private val firestore = FirebaseFirestore.getInstance()

    fun fetchData() {
        firestore.collection("Messages")
            .document(FirebaseAuth.getInstance().currentUser?.uid.toString())
            .collection("capsule")
            .get()
            .addOnSuccessListener {
                val messages = it.toObjects(CapsuleData::class.java)
                _messagelist.value = messages
            }
    }
}