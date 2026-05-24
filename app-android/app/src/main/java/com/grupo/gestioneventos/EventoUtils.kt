package com.grupo.gestioneventos

fun Map<String, Any>.intValue(key: String, default: Int = 0): Int {
    val value = this[key] ?: return default
    return when (value) {
        is Int -> value
        is Double -> value.toInt()
        is Float -> value.toInt()
        is Number -> value.toInt()
        else -> value.toString().toDoubleOrNull()?.toInt() ?: default
    }
}

fun Map<String, Any>.textValue(key: String): String = this[key]?.toString().orEmpty()
