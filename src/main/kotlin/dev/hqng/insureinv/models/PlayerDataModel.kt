package dev.hqng.insureinv.models

import java.util.*

data class PlayerDataModel(
    val uuid: UUID,
    var username: String,
    var charges: Int = 0,
    var protectionEnabled: Boolean = true,
    var totalChargesPurchased: Int = 0,
    var protectionActivations: Int = 0
) {
    fun hasCharges(): Boolean = charges > 0

    @Synchronized
    fun consumeCharge(): Boolean {
        return if (hasCharges()) {
            charges--
            protectionActivations++
            true
        } else {
            false
        }
    }

    @Synchronized
    fun addCharges(amount: Int) {
        charges += amount
        totalChargesPurchased += amount
    }

    @Synchronized
    fun updateCharges(amount: Int) {
        charges = amount
    }
}
