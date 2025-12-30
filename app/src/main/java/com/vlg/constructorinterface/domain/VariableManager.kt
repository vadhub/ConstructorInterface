package com.vlg.constructorinterface.domain

object VariableManager {
    val variables = mutableMapOf<String, Any>()

    fun set(name: String, value: Any) {
        variables[name] = value
    }

    fun getString(name: String): String? =
        variables[name]?.toString()

    fun getDouble(name: String): Double? =
        variables[name] as? Double ?: variables[name].toString().toDoubleOrNull()

    fun getInt(name: String): Int? =
        variables[name] as? Int ?: variables[name].toString().toIntOrNull()

    fun contains(name: String) = variables.containsKey(name)

    fun clear() = variables.clear()
}