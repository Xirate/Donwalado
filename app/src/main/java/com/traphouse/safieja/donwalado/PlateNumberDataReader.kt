package com.traphouse.safieja.donwalado

import android.content.res.Resources
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class PlateNumberDataReader {

    fun readPlateNumberDataFromJson(resources: Resources) : List<PlateNumberPairing> {
        resources.openRawResource(R.raw.pairings).bufferedReader().use {
            return Gson().fromJson(it,object: TypeToken<ArrayList<PlateNumberPairing>>() {}.type)
        }
    }
}