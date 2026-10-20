package com.example.directoriodeempresas.backend.repository

import android.content.ContentValues
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import com.example.directoriodeempresas.backend.database.DirectEmpresarialDbHelper
import com.example.directoriodeempresas.backend.database.DirectorioEmpresasContract
import com.example.directoriodeempresas.frontend.model.Classification
import com.example.directoriodeempresas.frontend.model.Company
import androidx.core.database.sqlite.transaction

class DirectEmpresarialRepository(context: Context) {
    private val db = DirectEmpresarialDbHelper.getInstance(context)

    suspend fun insertEmpresa(values: ContentValues): Long{
        return db.writableDatabase.insert(DirectorioEmpresasContract.EmpresaEntry.TABLE_NAME,
            null, values)
    }

    suspend fun insertProductosServicios(values: ContentValues): Long{
        return db.writableDatabase.insert(DirectorioEmpresasContract.ProductoServicioEntry.TABLE_NAME,
            null, values)
    }

    suspend fun retrieveEmpresas(): MutableList<Company> {
        val empresas: MutableList<Company> = mutableListOf<Company>()

        val cursor = db.readableDatabase.rawQuery("SELECT * FROM ${DirectorioEmpresasContract
            .EmpresaEntry.TABLE_NAME}", null)

        with(cursor) {
            while (moveToNext()) {
                val newCompany = Company(
                    name = getString(getColumnIndexOrThrow(DirectorioEmpresasContract
                        .EmpresaEntry.COLUMN_NAME_NOMBRE)),
                    classification = retrieveClassification(getString(getColumnIndexOrThrow(DirectorioEmpresasContract
                        .EmpresaEntry.COLUMN_NAME_CLASIFICACION))),
                    websiteUrl = getString(getColumnIndexOrThrow(DirectorioEmpresasContract
                        .EmpresaEntry.COLUMN_NAME_URL_PAGINA)),
                    phone = getString(getColumnIndexOrThrow(DirectorioEmpresasContract
                        .EmpresaEntry.COLUMN_NAME_TELEFONO)),
                    email = getString(getColumnIndexOrThrow(DirectorioEmpresasContract
                        .EmpresaEntry.COLUMN_NAME_EMAIL)),
                    productsAndServices = ""

                )
                empresas.add(newCompany)
            }
        }

        cursor.close()
        return empresas
    }

    suspend fun retrieveProductsServices(company: Company): String {
        val productosServicios: MutableList<String> = mutableListOf<String>()

        val cursor = db.readableDatabase.rawQuery("SELECT * FROM ${DirectorioEmpresasContract
            .ProductoServicioEntry.TABLE_NAME} WHERE ${DirectorioEmpresasContract
                .ProductoServicioEntry.COLUMN_NAME_TELEFONO_EMPRESA} = ${company.phone}", null)

        with(cursor) {
            while (moveToNext()) {
                val productService = getString(getColumnIndexOrThrow(DirectorioEmpresasContract
                        .ProductoServicioEntry.COLUMN_NAME_NOMBRE_PRODUCTO_SERVICIO))

                productosServicios.add(productService)
            }
        }
        cursor.close()
        val strProductsServices: String = productosServicios.joinToString(", ")

        return strProductsServices
    }

    /**
     * Actualiza los datos de una empresa identificada por su teléfono (PK, no se modifica).
     * Devuelve el número de filas actualizadas.
     */
    suspend fun updateEmpresa(company: Company): Int {
        return db.writableDatabase.update(
            DirectorioEmpresasContract.EmpresaEntry.TABLE_NAME,
            empresa2Entry(company),
            "${DirectorioEmpresasContract.EmpresaEntry.COLUMN_NAME_TELEFONO} = ?",
            arrayOf(company.phone)
        )
    }

    /**
     * Elimina la empresa y sus productos/servicios asociados.
     * Devuelve el número de empresas eliminadas.
     */
    suspend fun deleteEmpresa(phone: String): Int {
        return db.writableDatabase.transaction {
            // Primero los hijos, por si no hay ON DELETE CASCADE
            delete(
                DirectorioEmpresasContract.ProductoServicioEntry.TABLE_NAME,
                "${
                    DirectorioEmpresasContract.ProductoServicioEntry
                        .COLUMN_NAME_TELEFONO_EMPRESA
                } = ?",
                arrayOf(phone)
            )
            delete(
                DirectorioEmpresasContract.EmpresaEntry.TABLE_NAME,
                "${DirectorioEmpresasContract.EmpresaEntry.COLUMN_NAME_TELEFONO} = ?",
                arrayOf(phone)
            )

        }
    }

    suspend fun updateProductsServices(company: Company): Boolean {
        db.writableDatabase.transaction {
            delete(
                DirectorioEmpresasContract.ProductoServicioEntry.TABLE_NAME,
                "${DirectorioEmpresasContract.ProductoServicioEntry
                    .COLUMN_NAME_TELEFONO_EMPRESA} = ?",
                arrayOf(company.phone)
            )

            for (values in productsServices2Entry(company)) {
                insert(
                    DirectorioEmpresasContract.ProductoServicioEntry.TABLE_NAME,
                    null,
                    values
                )
            }
        }
        return true
    }

    fun retrieveClassification(classification: String): Classification {
        return when (classification) {
            "Consultoría", "Consultory" -> Classification.Consultory
            "Desarrollo a la medida", "CustomDevelopment" -> Classification.CustomDevelopment
            "Fábrica de software", "SoftwareFactory" -> Classification.SoftwareFactory
            else -> Classification.Consultory
        }
    }

    fun empresa2Entry(company: Company): ContentValues {
        val values = ContentValues().apply {
            put(DirectorioEmpresasContract.EmpresaEntry.COLUMN_NAME_TELEFONO, company.phone)
            put(DirectorioEmpresasContract.EmpresaEntry.COLUMN_NAME_NOMBRE, company.name)
            put(DirectorioEmpresasContract.EmpresaEntry.COLUMN_NAME_URL_PAGINA, company.websiteUrl)
            put(DirectorioEmpresasContract.EmpresaEntry.COLUMN_NAME_EMAIL, company.email)
            put(DirectorioEmpresasContract.EmpresaEntry.COLUMN_NAME_CLASIFICACION, company
                .classification?.name)
        }

        return values
    }

    fun productsServices2Entry(company: Company): MutableList<ContentValues> {
        val productsServicesList: List<String> = company.productsAndServices.split(",")
        val listEntries: MutableList<ContentValues> = mutableListOf()

        for (prodServ in productsServicesList) {
            val values = ContentValues().apply {
                put(DirectorioEmpresasContract.ProductoServicioEntry
                    .COLUMN_NAME_NOMBRE_PRODUCTO_SERVICIO, prodServ.trim())
                put(DirectorioEmpresasContract.ProductoServicioEntry
                    .COLUMN_NAME_TELEFONO_EMPRESA, company.phone)
            }
            listEntries.add(values)
        }

        return listEntries
    }
}