package com.example.directoriodeempresas.backend.database

import android.provider.BaseColumns

const val SQL_CREATE_EMPRESA =
    "CREATE TABLE ${DirectorioEmpresasContract.EmpresaEntry.TABLE_NAME} (" +
            "${DirectorioEmpresasContract.EmpresaEntry.COLUMN_NAME_TELEFONO} INTEGER PRIMARY KEY," +
            "${DirectorioEmpresasContract.EmpresaEntry.COLUMN_NAME_NOMBRE} TEXT," +
            "${DirectorioEmpresasContract.EmpresaEntry.COLUMN_NAME_URL_PAGINA} TEXT," +
            "${DirectorioEmpresasContract.EmpresaEntry.COLUMN_NAME_EMAIL} TEXT," +
            "${DirectorioEmpresasContract.EmpresaEntry.COLUMN_NAME_CLASIFICACION} TEXT)"

const val SQL_DELETE_EMPRESA = "DROP TABLE IF EXISTS ${DirectorioEmpresasContract
    .EmpresaEntry.TABLE_NAME}"

const val SQL_CREATE_PRODUCTO_SERVICIO =
    "CREATE TABLE ${DirectorioEmpresasContract.ProductoServicioEntry.TABLE_NAME} (" +
            "${BaseColumns._ID} INTEGER PRIMARY KEY," +
            "${DirectorioEmpresasContract.ProductoServicioEntry.COLUMN_NAME_TELEFONO_EMPRESA} INTEGER," +
            "${DirectorioEmpresasContract.ProductoServicioEntry.COLUMN_NAME_NOMBRE_PRODUCTO_SERVICIO} TEXT," +
            "FOREIGN KEY(${DirectorioEmpresasContract.ProductoServicioEntry.COLUMN_NAME_TELEFONO_EMPRESA}) REFERENCES ${DirectorioEmpresasContract
                .EmpresaEntry.TABLE_NAME}(${DirectorioEmpresasContract.EmpresaEntry.COLUMN_NAME_TELEFONO})" +
            "ON DELETE CASCADE" +
            " ON UPDATE NO ACTION)"

const val SQL_DELETE_PRODUCTO_SERVICIO = "DROP TABLE IF EXISTS ${DirectorioEmpresasContract
    .ProductoServicioEntry.TABLE_NAME}"