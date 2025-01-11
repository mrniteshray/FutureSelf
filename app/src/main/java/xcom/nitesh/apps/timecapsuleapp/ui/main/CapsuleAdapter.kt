package xcom.nitesh.apps.timecapsuleapp.ui.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import xcom.nitesh.apps.timecapsuleapp.Model.CapsuleData
import xcom.nitesh.apps.timecapsuleapp.R

class CapsuleAdapter(val caplist : List<CapsuleData>, private val onClick: (CapsuleData) -> Unit) : RecyclerView.Adapter<CapsuleAdapter.CapsuleViewHolder>() {

    class CapsuleViewHolder(itemview : View) : RecyclerView.ViewHolder(itemview){
        val title : TextView = itemview.findViewById(R.id.tvCapsuleTitle)
        val unlockDate : TextView = itemview.findViewById(R.id.unlockDateTV)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CapsuleViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.itemview_capsule,parent,false)
        return CapsuleViewHolder(view)
    }

    override fun getItemCount(): Int {
        return caplist.size
    }

    override fun onBindViewHolder(holder: CapsuleViewHolder, position: Int) {
        var current = caplist[position]
        holder.title.text = current.title
        holder.unlockDate.text = "Unlock & Notify on : "+current.unlockDate

        if(current.isUnlocked){
            holder.itemView.setOnClickListener {
                onClick(current)
            }
        } else{
            holder.itemView.setOnClickListener{
                Toast.makeText(holder.itemView.context, "Message is locked", Toast.LENGTH_SHORT).show()
            }
        }
    }
}