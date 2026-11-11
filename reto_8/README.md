# Folio – solo capa visual (Jetpack Compose)

Copia `app/src/main` (java + res) a un proyecto "Empty Activity" con package `com.folio`.
No hay ViewModels, repositorio, DI ni navegación: cada pantalla recibe estado y lambdas.
Abre `ui/preview/Previews.kt` y usa el panel Preview/Split de Android Studio.

## Dependencias mínimas
- `androidx.compose:compose-bom` + `material3`, `ui`, `material-icons-extended`
- `ui-tooling-preview` (implementation) y `ui-tooling` (debugImplementation)
- Plugin `org.jetbrains.kotlin.plugin.compose` (Kotlin 2.x)

Activa R8 (`minifyEnabled`) en release para recortar los iconos que no uses.

## Pantallas (stateless)
| Diseño | Composable |
|---|---|
| Company directory / Company deleted successfully | `CompanyListScreen` |
| Company details | `CompanyDetailScreen` |
| Delete company confirmation | `DeleteConfirmationContent` (dentro de `ModalBottomSheet`) |
| Add company / Edit company | `CompanyFormScreen` |
| Filtered company results | `CompanyFilterScreen` |
