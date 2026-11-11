package com.example.directoriodeempresas.frontend

import androidx.annotation.StringRes
import com.example.directoriodeempresas.R
import com.example.directoriodeempresas.frontend.model.Classification

/** Keeps the domain layer free of Android resources. */
@get:StringRes
val Classification.labelRes: Int
    get() = when (this) {
        Classification.Consultory -> R.string.class_consultory
        Classification.CustomDevelopment -> R.string.class_custom_development
        Classification.SoftwareFactory -> R.string.class_software_factory
    }

