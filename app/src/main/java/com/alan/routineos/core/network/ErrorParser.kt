package com.alan.routineos.core.network

import com.google.gson.Gson
import retrofit2.HttpException
import java.io.IOException

fun Throwable.toApiError(): ApiError {

    return when (this) {

        is HttpException -> {

            try {

                val errorBody =
                    response()?.errorBody()?.string()

                val apiError = Gson().fromJson(
                    errorBody,
                    ApiError::class.java
                )

                apiError ?: ApiError(
                    code = "UNKNOWN_ERROR",
                    message = "Ocurrió un error inesperado"
                )

            } catch (_: Exception) {

                ApiError(
                    code = "PARSE_ERROR",
                    message = "No pudimos procesar la respuesta"
                )
            }
        }

        is IOException -> {

            ApiError(
                code = "NO_CONNECTION",
                message = "Sin conexión a internet"
            )
        }

        else -> {

            ApiError(
                code = "UNKNOWN_ERROR",
                message = "Ocurrió un error inesperado"
            )
        }
    }
}