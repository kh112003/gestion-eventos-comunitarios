package com.grupo.gestioneventos.network


import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    @POST("auth/login")
    suspend fun login(@Body body: Map<String, String>): Response<Map<String, Any>>

    @POST("auth/google")
    suspend fun loginGoogle(@Body body: Map<String, String>): Response<Map<String, Any>>

    @POST("auth/register")
    suspend fun register(@Body body: Map<String, String>): Response<Map<String, Any>>

    @GET("eventos")
    suspend fun getEventos(): Response<List<Map<String, Any>>>

    @GET("eventos/{id}")
    suspend fun getEvento(@Path("id") id: Int): Response<Map<String, Any>>

    @POST("eventos")
    suspend fun createEvento(@Body body: Map<String, @JvmSuppressWildcards Any>): Response<Map<String, Any>>

    @PUT("eventos/{id}")
    suspend fun updateEvento(
        @Path("id") id: Int,
        @Body body: Map<String, @JvmSuppressWildcards Any>
    ): Response<Map<String, Any>>

    @DELETE("eventos/{id}")
    suspend fun deleteEvento(@Path("id") id: Int): Response<Map<String, Any>>

    @POST("asistencias")
    suspend fun confirmarAsistencia(@Body body: Map<String, Int>): Response<Map<String, Any>>

    @GET("historial/usuario/{id}")
    suspend fun getHistorial(@Path("id") id: Int): Response<List<Map<String, Any>>>
}
