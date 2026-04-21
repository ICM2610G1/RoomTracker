# Diagnóstico de Arquitectura — RoomTracker

## Patrón actual: MVVM con Jetpack Compose

El proyecto sigue el patrón **MVVM (Model-View-ViewModel)** con Compose, que es el correcto para Android moderno. La estructura de paquetes es reconocible y tiene sentido general.

---

## Estructura de paquetes

```
roomtracker/
├── MainActivity.kt
├── RoomTrackerApp.kt
├── SupabaseClient.kt
├── SharedPrefsSessionManager.kt
├── data/
│   └── SensorRepository.kt
├── model/
│   ├── Institucion.kt
│   └── OrientationData.kt
├── map/
│   ├── AStar.kt
│   ├── CampusLayer.kt
│   ├── GraphLoader.kt
│   ├── GraphNode.kt
│   ├── GraphUtils.kt
│   └── KShortestPaths.kt
├── navigation/
│   └── Navigation.kt
├── ui/
│   ├── screens/      (17 pantallas)
│   ├── components/
│   │   ├── auth/     (13 componentes)
│   │   ├── common/   (3 componentes)
│   │   └── map/      (9 componentes)
│   └── theme/
├── viewmodel/
│   ├── AuthViewModel.kt
│   └── SensorViewModel.kt
└── utils/
    └── MapUtils.kt
```

---

## Lo que está bien

| Aspecto | Detalle |
|---|---|
| MVVM aplicado | ViewModels con StateFlow, UI reactiva con collectAsStateWithLifecycle |
| Componentes pequeños | auth/ y common/ tienen componentes bien separados y reutilizables |
| Algoritmos aislados | AStar, KShortestPaths, GraphUtils están en su propio paquete |
| Navigation centralizada | Una sola clase maneja todas las rutas |
| Theme consistente | Colores y tipografía separados del resto de la UI |
| Retry logic | Login y registro reintentan automáticamente ante errores de red |
| Session persistence | SharedPrefsSessionManager persiste la sesión entre reinicios |

---

## Problemas detectados

### Crítico

**1. Credenciales hardcodeadas en `SupabaseClient.kt`**
```kotlin
supabaseUrl = "https://aqlexalyccmmjspijyrz.supabase.co"
supabaseKey = "sb_publishable_H8K0bX5q0QHgUb5td2wYwQ_rpkd_7ZL"
```
Las claves están visibles en el código fuente y dentro del APK compilado.  
**Solución sugerida:** moverlas a `local.properties` + `BuildConfig`.

---

### Serios

**2. `HomeMapScreen.kt` hace demasiado (681 líneas)**

Una sola pantalla está manejando:
- Ubicación GPS en tiempo real
- Carga del grafo y geometrías
- Renderizado de polígonos y polylines
- Lógica de búsqueda de rutas con A*
- Zoom y cámara del mapa
- Bottom sheet interactivo
- Snapping a nodos del grafo

Debería existir un `MapViewModel` que centralice el estado y la lógica.

---

**3. `AcademicStatsScreen.kt` es la más grande del proyecto (895 líneas)**

Mezcla: UI, cálculo de promedios, data classes locales, múltiples composables dentro del mismo archivo. Los datos están hardcodeados y no vienen de Supabase.

---

**4. `AuthViewModel.kt` mezcla dos responsabilidades (~500 líneas)**

Maneja autenticación (login, registro, OTP, reset) **y** descarga de archivos del mapa (`downloadMapFiles`). Son responsabilidades distintas que deberían estar en ViewModels separados.

Además, la descarga de archivos se lanza con `launch { }` (fire-and-forget) después de marcar el login como `Success`, por lo que si falla la descarga, el usuario llega al mapa sin archivos.

```kotlin
_loginState.value = AuthState.Success   // ← se marca exitoso
launch { downloadMapFiles(...) }        // ← pero esto puede fallar silenciosamente
```

---

**5. No hay capa Repository para datos de Supabase**

Los ViewModels acceden directamente a `supabase.from("tabla").select()`. Esto significa:
- No se puede testear sin una conexión real a Supabase
- Si cambia el backend, hay que modificar los ViewModels directamente
- Código de queries disperso sin centralización

---

**6. Lógica de reintentos duplicada**

`login()` y `register()` repiten el mismo bloque `repeat(3) { ... }` con la misma lógica. Debería extraerse a una función `withRetry()`.

---

**7. No hay inyección de dependencias**

Todo se instancia manualmente. Los ViewModels crean sus propias dependencias. Esto hace muy difícil escribir tests unitarios.

---

### Moderados

**8. `MapUtils.kt` duplica `distanceMeters()`**

Existe en `MapUtils.kt` y en `GraphUtils.kt` con implementaciones ligeramente distintas.

**9. Estado volátil en variables miembro del ViewModel**

```kotlin
private var pendingEmail = ""
private var pendingNombre = ""
private var pendingApellido = ""
```
Si el ViewModel se recrea, estos valores se pierden. Deberían estar en `SavedStateHandle` o en un `StateFlow`.

**10. Sensores sin manejo de ciclo de vida**

`SensorViewModel.startSensors()` / `stopSensors()` se llaman desde `HomeMapScreen`, pero no están vinculados al ciclo de vida de la pantalla. Los sensores podrían quedar activos si la pantalla se destruye.

---

### Menores

**11. Logs hardcodeados con `android.util.Log`**
```kotlin
android.util.Log.d("RT_NAV", "sessionChecked=$sessionChecked")
```
Dispersos por todo el código. En producción deberían desactivarse o usarse una librería como **Timber**.

**12. Magic numbers sin documentación**
```kotlin
0.5   // en GraphUtils.nearestNode()
3.0   // en KShortestPaths
200.0 // en penalizaciones de rutas
```
Sin comentarios que expliquen qué representan estos valores.

**13. `RememberMeRow` sin funcionalidad real**

El checkbox "Recuérdame" existe en la UI pero no hace nada.

---

## Técnicas y patrones recomendados para mejorar

| Problema | Técnica sugerida |
|---|---|
| Sin Repository | Aplicar **Repository Pattern**: `AuthRepository`, `EventosRepository`, `MapRepository` |
| Sin DI | Implementar **Hilt** (DI oficial de Android) para inyectar repos en ViewModels |
| ViewModels grandes | Aplicar **Single Responsibility**: un ViewModel por funcionalidad |
| Sin Use Cases | Agregar capa de **Use Cases / Interactors** entre ViewModel y Repository |
| Credenciales expuestas | Usar `local.properties` + **BuildConfig** |
| Sin tests | Con Hilt + Repository se pueden escribir **unit tests** con mocks |
| Logs en producción | Reemplazar `android.util.Log` con **Timber** |
| Estado volátil | Usar **SavedStateHandle** en ViewModels |
| Fire-and-forget | Usar `combine()` de Flow o estado `Loading` para la descarga de mapas |

---

## Arquitectura recomendada (Clean Architecture lite)

```
UI (Screens/Composables)
    ↓
ViewModel  (estado + lógica de presentación)
    ↓
Use Cases  (lógica de negocio)
    ↓
Repository (abstracción de datos)
    ↓
Data Sources (Supabase, SensorManager, SharedPrefs)
```

---

## Resumen ejecutivo

| Categoría | Estado actual |
|---|---|
| Patrón arquitectónico | MVVM ✅ aplicado correctamente en general |
| Separación de capas | ⚠️ Buena en UI, débil en datos |
| Tamaño de archivos | ⚠️ 2 pantallas críticas (+600 líneas) |
| Seguridad | ❌ Credenciales expuestas |
| Testabilidad | ❌ Muy baja sin DI ni repositories |
| Reutilización | ✅ Buena en componentes UI |
| Algoritmos de mapa | ✅ Bien aislados y organizados |
| Manejo de errores | ⚠️ Disperso, no centralizado |
