package com.example.directoriodeempresas.frontend

import android.app.Application
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.compose.NavHost
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.directoriodeempresas.frontend.detail.CompanyDetailRoute
import com.example.directoriodeempresas.frontend.filter.CompanyFilterRoute
import com.example.directoriodeempresas.frontend.form.CompanyFormRoute
import com.example.directoriodeempresas.frontend.form.CompanyFormViewModel
import com.example.directoriodeempresas.frontend.list.CompanyListRoute

enum class DirectEmpresaNav() {
    Directorio,
    AnadirEmpresa,
    DetallesEmpresa,
    EditarEmpresa,
    Filtrar
}
@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@Composable
fun ScreenApp(
    navController: NavHostController = rememberNavController()
) {
    val sharedViewModel: SharedViewModel = viewModel()
    NavHost(
        navController = navController,
        startDestination = DirectEmpresaNav.Directorio.name
    ) {
        composable(route = DirectEmpresaNav.Directorio.name) {
            CompanyListRoute(
                onAddCompany = { navController.navigate(DirectEmpresaNav
                .AnadirEmpresa.name) },
                sharedViewModel = sharedViewModel,
                navController = navController,
                onOpenFilter = { navController.navigate(DirectEmpresaNav.Filtrar.name) }
            )
        }
        composable(route = DirectEmpresaNav.AnadirEmpresa.name) {
            val formViewModel: CompanyFormViewModel = viewModel(
                factory = viewModelFactory {
                    initializer {
                        // 1. Recuperamos la Application del sistema automáticamente
                        val app = this[APPLICATION_KEY] as Application

                        // 2. Retornamos el ViewModel con ambos parámetros
                        CompanyFormViewModel(application = app)
                    }
                }
            )
            CompanyFormRoute(onClose = { navController.navigate(DirectEmpresaNav
                .Directorio.name) },
                viewModel = formViewModel
            )
        }
        composable(route = DirectEmpresaNav.DetallesEmpresa.name) {
            CompanyDetailRoute(
                sharedViewModel = sharedViewModel,
                onBack = { navController.navigate(DirectEmpresaNav
                    .Directorio.name) },
                onEdit = { navController.navigate(DirectEmpresaNav
                    .EditarEmpresa.name) }
            )
        }
        composable(route = DirectEmpresaNav.EditarEmpresa.name) {
            val formViewModel: CompanyFormViewModel = viewModel(
                factory = viewModelFactory {
                    initializer {
                        // 1. Recuperamos la Application del sistema automáticamente
                        val app = this[APPLICATION_KEY] as Application

                        // 2. Retornamos el ViewModel con ambos parámetros
                        CompanyFormViewModel(
                            application = app,
                            isEditing = true,
                            name = sharedViewModel.sharedCompany.name,
                            classification = sharedViewModel.sharedCompany.classification,
                            website = sharedViewModel.sharedCompany.websiteUrl,
                            phone = sharedViewModel.sharedCompany.phone,
                            email = sharedViewModel.sharedCompany.email,
                            products = sharedViewModel.sharedCompany.productsAndServices,
                        )
                    }
                }
            )

            CompanyFormRoute(
                onClose = { navController.navigate(DirectEmpresaNav
                    .DetallesEmpresa.name) },
                viewModel = formViewModel
            )
        }
        composable(DirectEmpresaNav.Filtrar.name) {
            CompanyFilterRoute(onBack = { navController.popBackStack() })
        }
    }
}