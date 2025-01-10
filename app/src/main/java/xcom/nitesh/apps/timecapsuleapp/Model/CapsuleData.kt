package xcom.nitesh.apps.timecapsuleapp.Model

import java.io.Serializable

data class CapsuleData(
    val title: String,
    val content: String,
    val createdDate: String,
    val unlockDate: String,
    val isUnlocked: Boolean
){
    constructor() : this("", "", "", "", false)

}