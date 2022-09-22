package com.example.weatherforecast.DBase

class CityRepository(private val dao : CityDAO) {

    val cities = dao.getAllCity()

    suspend fun insert(manageCities: ManageCities){
        dao.insertCity(manageCities)
    }

    //   suspend fun update(manageCities: ManageCities){
    //     dao.updateCity(manageCities)
    // }

    suspend fun delete(manageCities: ManageCities){
        dao.deleteCity(manageCities)
    }

    suspend fun deleteAll(){
        dao.deleteAll()
    }
}