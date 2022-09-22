package com.example.weatherforecast.utilities

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.weatherforecast.DBase.ManageCities
import com.example.weatherforecast.R
import com.example.weatherforecast.databinding.CityListItemBinding


class CityAdapter (private val citylist: List<ManageCities>
                   ,private val clickListener:(ManageCities)->Unit) : RecyclerView.Adapter<MyViewHolder>()
{
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding : CityListItemBinding =
            DataBindingUtil.inflate(layoutInflater, R.layout.city_list_item,parent,false)
        return MyViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return citylist.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(citylist[position],clickListener)
    }

}

class MyViewHolder(val binding: CityListItemBinding):RecyclerView.ViewHolder(binding.root){

    fun bind(manageCities: ManageCities,clickListener:(ManageCities)->Unit){
        binding.nameTextView.text = manageCities.CityName
        binding.listItemLayout.setOnClickListener{
            clickListener(manageCities)
        }
    }
}