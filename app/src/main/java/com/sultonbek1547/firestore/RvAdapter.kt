package com.sultonbek1547.firestore

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.sultonbek1547.firestore.databinding.RvItemBinding

class RvAdapter(val list: ArrayList<Person>, val rvClick: RvClick) : RecyclerView.Adapter<RvAdapter.Vh>() {
    inner class Vh(private val itemRvItemBinding: RvItemBinding) :
        ViewHolder(itemRvItemBinding.root) {
        fun onBind(person: Person) {
            itemRvItemBinding.apply {
                tvName.text = person.firstName
                tvSurname.text = person.lastName
                tvAge.text = person.age.toString()
            }
            itemRvItemBinding.root.setOnClickListener {
                rvClick.itemClicked(person)
            }
            itemRvItemBinding.root.setOnLongClickListener {
                rvClick.itemLongClicked(person)
                true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Vh {
        return Vh(RvItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: Vh, position: Int) {
        holder.onBind(list[position])
    }
}

interface RvClick {
    fun itemLongClicked(person: Person)
    fun itemClicked(person: Person)
}
