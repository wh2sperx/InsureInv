package dev.hqng.insureinv.storages

enum class StorageType {
    SQLITE, MYSQL, JSON;

    companion object {
        fun fromString(value: String?): StorageType {
            return when (val upper = value?.uppercase()) {
                "SQLITE" -> SQLITE
                "MYSQL" -> MYSQL
                "JSON" -> JSON
                else -> SQLITE
            }
        }
    }
}