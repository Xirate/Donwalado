package com.traphouse.safieja.donwalado

class PlateNumberPairingRepository {
    val plateNumberMappings: HashMap<String, PlateNumberPairing> = HashMap()

    fun populateMappingsFromList(list: List<PlateNumberPairing>) {
        list.forEach {
            plateNumberMappings[it.code] = it
        }
    }
}