package com.example.directoriodeempresas.backend.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DirectEmpresarialDbHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME,
    null, DATABASE_VERSION) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(SQL_CREATE_EMPRESA)
        db.execSQL(SQL_CREATE_PRODUCTO_SERVICIO)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL(SQL_DELETE_EMPRESA)
        db.execSQL(SQL_DELETE_PRODUCTO_SERVICIO)
        onCreate(db)
    }

    companion object {
        const val DATABASE_VERSION = 1
        const val DATABASE_NAME = "DirectEmpresarial.db"

        @Volatile private var INSTANCE: DirectEmpresarialDbHelper? = null

        fun getInstance(context: Context): DirectEmpresarialDbHelper = INSTANCE ?: synchronized(this) {
            INSTANCE ?: DirectEmpresarialDbHelper(context.applicationContext).also { INSTANCE = it}
        }
    }

}