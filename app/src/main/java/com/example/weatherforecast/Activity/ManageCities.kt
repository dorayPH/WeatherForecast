package com.example.weatherforecast.Activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.weatherforecast.DBase.CityDatabase
import com.example.weatherforecast.DBase.CityRepository
import com.example.weatherforecast.DBase.ManageCities
import com.example.weatherforecast.Models.CityViewModel
import com.example.weatherforecast.Models.CityViewModelFactory
import com.example.weatherforecast.R
import com.example.weatherforecast.databinding.ActivityManageCitiesBinding
import com.example.weatherforecast.utilities.CityAdapter
import com.example.weatherforecast.utilities.Constants

class ManageCities : AppCompatActivity() {
    private lateinit var binding: ActivityManageCitiesBinding
    private lateinit var cityViewModel: CityViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //setContentView(R.layout.activity_manage_cities)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_manage_cities)
        val dao = CityDatabase.getInstance(application).cityDAO
        val repository = CityRepository(dao)
        val factory = CityViewModelFactory(repository)
        cityViewModel = ViewModelProvider(this,factory)[CityViewModel::class.java]
        binding.myViewModel = cityViewModel
        binding.lifecycleOwner = this
        initRecyclerView()

        binding.viewData.setOnClickListener(){
            Constants.Location = binding.nameText.text.toString()

            val intent = Intent(this@ManageCities, MainActivity::class.java)
            startActivity(intent)
        }
    }

    private fun initRecyclerView(){
        binding.cityRecyclerView.layoutManager = LinearLayoutManager(this)
        displayCityList()
    }

    private fun displayCityList(){
        cityViewModel.cities.observe(this, Observer {
            Log.i("MYTAG",it.toString())
            binding.cityRecyclerView.adapter = CityAdapter(it,{selectedItem:ManageCities->listItemClicked(selectedItem)})
        })
    }

    private fun listItemClicked(manageCities: com.example.weatherforecast.DBase.ManageCities){
        cityViewModel.initUpdateAndDelete(manageCities)
    }
}