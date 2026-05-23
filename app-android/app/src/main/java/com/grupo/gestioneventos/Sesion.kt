package com.grupo.gestioneventos

import android.content.Context

object Sesion {
    private const val PREFS = "sesion"
    private const val KEY_USUARIO_ID = "usuario_id"
    private const val KEY_NOMBRE = "nombre"
    private const val KEY_EMAIL = "email"

    fun guardar(context: Context, usuarioId: Int, nombre: String, email: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putInt(KEY_USUARIO_ID, usuarioId)
            .putString(KEY_NOMBRE, nombre)
            .putString(KEY_EMAIL, email)
            .apply()
    }

    fun usuarioId(context: Context): Int =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getInt(KEY_USUARIO_ID, 1)

    fun cerrar(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().clear().apply()
    }
}
