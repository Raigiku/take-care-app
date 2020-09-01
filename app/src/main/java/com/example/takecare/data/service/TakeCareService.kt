package com.example.takecare.data.service

import com.example.takecare.data.api.request.*
import com.example.takecare.data.api.response.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST


interface TakeCareService {

    @POST("/auth/login")
    suspend fun login(@Body loginRequest: LoginRequest): Response<LoginResponse>

    @POST("/auth/refresh")
    fun refreshToken(@Body refreshTokenRequest: RefreshTokenRequest): retrofit2.Call<RefreshTokenResponse>

    @POST("/auth/request/recover")
    suspend fun requestRecoverPassword(@Body requestRecoverPasswordRequest: RequestRecoverPasswordRequest): Response<RecoverPasswordResponse>

    @POST("/auth/recoverpass")
    suspend fun recoverPassword(@Body recoverPasswordRequest: RecoverPasswordRequest): Response<RecoverPasswordResponse>

    @POST("/users/register")
    suspend fun register(@Body registerRequest: RegisterRequest): Response<RegisterResponse>

    @GET("/diagnostics/get")
    suspend fun getDiagnostics() : Response<GetDiagnosticsResponse>

    @GET("/admin/users/add")
    suspend fun addUser(@Header("Authorization") token: String)
}