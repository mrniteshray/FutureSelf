package xcom.nitesh.apps.timecapsuleapp.data.Model

import java.io.Serializable

data class CapsuleData(
    val title: String,
    val content: String,
    val createdDate: String,
    val unlockDate: String,
    val unlockTime: String,
    var isUnlocked: Boolean
){
    constructor() : this("", "", "", "", "",false)

}