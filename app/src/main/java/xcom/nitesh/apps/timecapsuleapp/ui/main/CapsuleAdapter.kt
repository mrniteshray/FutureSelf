package xcom.nitesh.apps.timecapsuleapp.ui.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import xcom.nitesh.apps.timecapsuleapp.Model.Capsule
import xcom.nitesh.apps.timecapsuleapp.R

class CapsuleAdapter(val caplist : List<Capsule>) : RecyclerView.Adapter<CapsuleAdapter.CapsuleViewHolder>() {

    class CapsuleViewHolder(itemview : View) : RecyclerView.ViewHolder(itemview){
        val title : TextView = itemview.findViewById(R.id.tvCapsuleContent)
        val desc : TextView = itemview.findViewById(R.id.tvAdditionalInfo)
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
        holder.desc.text = current.desc
    }
}