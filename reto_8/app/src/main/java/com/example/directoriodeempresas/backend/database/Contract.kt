package com.example.directoriodeempresas.backend.database

import android.provider.BaseColumns

object DirectorioEmpresasContract {
    object EmpresaEntry : BaseColumns {
        const val TABLE_NAME = "empresa"
        const val COLUMN_NAME_TELEFONO = "telefono"
        const val COLUMN_NAME_NOMBRE = "nombre"
        const val COLUMN_NAME_URL_PAGINA = "url_pagina"
        const val COLUMN_NAME_EMAIL = "email"
        const val COLUMN_NAME_CLASIFICACION = "clasificacion"
    }

    object ProductoServicioEntry : BaseColumns {
        const val TABLE_NAME = "producto_servicio"
        const val COLUMN_NAME_TELEFONO_EMPRESA = "telefono_emepresa"
        const val COLUMN_NAME_NOMBRE_PRODUCTO_SERVICIO = "nombre_producto_servicio"
    }
}