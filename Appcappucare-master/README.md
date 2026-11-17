# 💊 Cappucare — Control Inteligente de Insumos Médicos

> Aplicación móvil Android para controlar existencias y mantener la **trazabilidad de los movimientos de bodega** de una empresa de venta de insumos médicos.

---

## 👥 Integrantes

| Nombre | Rol |
|---|---|
| **Benjamín Rojas** | Desarrollador |
| **Diego Sanzana** | Desarrollador |

## 🎓 Información académica

| | |
|---|---|
| **Asignatura** | DESARROLLO DE APLICACIONES MÓVILES_004D |
| **Código** | DSY1105 |
| **Evaluación** | Proyecto semestral · Unidad 2 · Evaluación Parcial 2 |
| **Proyecto** | **Cappucare** (`cl.duoc.cappucare`) |

---

## 📌 Contexto del negocio

**Cappucare** es una empresa que vende insumos médicos (guantes, gasas, instrumental, equipos de protección, etc.).
En este rubro un quiebre de stock impide atender pedidos de clínicas y pacientes, y un exceso de stock inmoviliza dinero.

La aplicación resuelve cuatro problemas concretos:

| Problema | Cómo lo resuelve la app |
|---|---|
| No se sabe cuánto hay ni dónde está | Inventario en tiempo real con ubicación en bodega y foto de cada insumo |
| Los quiebres de stock se detectan tarde | Alertas visuales y **notificación del sistema** al llegar al nivel mínimo |
| No hay trazabilidad | Cada ingreso, venta o merma queda registrada con fecha, motivo y responsable |
| Errores de digitación | Validaciones: no se puede vender más de lo que hay ni ingresar cantidades inválidas |

---

## ✨ Funcionalidades

### 📦 Inventario
- Listado de insumos en tarjetas con foto, código, categoría, ubicación y stock.
- Búsqueda por nombre, código o categoría.
- Filtro de **stock crítico** y banner animado con la cantidad de insumos en alerta.
- Crear, editar y eliminar insumos (con confirmación).
- Barra de progreso del stock; la tarjeta se tiñe de rojo y pulsa cuando el stock es crítico.

### 📝 Formulario de insumo
- Campos: código/ID, nombre, categoría, stock actual, nivel mínimo, unidad de medida, ubicación en bodega y foto.
- **Escáner de códigos de barras / QR** (CameraX + ML Kit): si el código ya existe, carga el insumo para editarlo.
- **Cámara** (`TakePicture`) y **galería** (`GetContent`) con `FileProvider`.
- Validaciones con mensajes de error e íconos en cada campo.

### 🔄 Registro de movimientos
- Tipos: **Ingreso** (recepción), **Venta** y **Merma**.
- Actualiza el stock y guarda el movimiento en **una sola transacción** (si algo falla, no se guarda nada).
- Una salida nunca puede superar el stock disponible.
- Responsable: Bodeguero, Vendedor o Administrador. La fecha se registra automáticamente.

### 🕑 Historial
- Trazabilidad completa: insumo, tipo, cantidad, motivo, responsable y fecha.
- Ingresos en verde y salidas en rojo.

### 🔔 Notificaciones locales
- Aviso del sistema cuando un insumo llega a su nivel mínimo, con nombre, unidades restantes y ubicación. Al tocarla abre la app.

### 📱 Diseño adaptable (`WindowSizeClass`)
| Pantalla | Navegación | Columnas |
|---|---|---|
| Compacta (teléfono) | `NavigationBar` inferior | 1 |
| Media (tablet pequeña) | `ModalNavigationDrawer` | 2 |
| Expandida (tablet grande) | `PermanentNavigationDrawer` | 3 |

### 🎬 Animaciones y retroalimentación visual
- Transiciones entre pantallas (fundido + desplazamiento; el formulario entra desde abajo).
- `Crossfade` entre estados cargando / vacío / error / lista.
- `AnimatedVisibility` en banner de alertas, barra inferior y escáner.
- Botón con rebote, progreso y cambio a verde al confirmar.
- Overlay de confirmación con check animado, o alerta pulsante si el stock quedó crítico.

---

## 🏗️ Arquitectura (MVVM)

```
Pantalla (Compose) → ViewModel → Repositorio → DAO / Room (SQLite)
       ↑                 │
       └── StateFlow ────┘   (la UI observa el estado y se redibuja sola)
```

### Estructura del proyecto

```
app/src/main/java/cl/duoc/cappucare/
├── MainActivity.kt              # Punto de entrada, permiso de notificaciones
├── CappucareApplication.kt      # Crea el AppContainer (inyección manual)
├── model/                       # Modelos, UI State, DataState, errores, mapeos
├── data/local/                  # Room: Entities, DAOs, AppDatabase
├── repository/                  # CappucareRepository, AppContainer
├── viewmodel/                   # ViewModels con StateFlow + validaciones
├── navigation/                  # Screen (sealed class), AppNavHost, CappucareApp
├── notification/                # StockNotifier
└── ui/
    ├── screens/                 # Inventario, Formulario, Movimiento, Historial
    ├── components/              # Campos, tarjetas, botón animado, escáner, alertas
    ├── utils/                   # WindowSizeClasses, ImageUtils, Formatos
    └── theme/                   # Color, Theme, Type
```

### Decisiones de diseño
- **State Hoisting:** cada pantalla tiene una versión *Stateful* (conectada al ViewModel) y otra *Stateless* (solo dibuja).
- **Validación 100 % en el ViewModel:** la UI solo observa el `StateFlow` y muestra `isError` + `supportingText`.
- **Inyección manual de dependencias** mediante `AppContainer` y `AppViewModelProvider`.
- **Rutas seguras** con `sealed class Screen(val route: String)`.

---

## 🗄️ Modelo de datos (Room)

**`productos`**: `codigo` (PK), `nombre`, `categoria`, `stockActual`, `stockMinimo`, `unidadMedida`, `ubicacion`, `fotoUri`.
Un insumo está en **stock crítico** cuando `stockActual <= stockMinimo`.

**`movimientos`**: `id` (PK autogenerado), `productoCodigo` (FK → `productos`, `ON DELETE CASCADE`), `tipo`, `cantidad`, `fecha`, `motivo`, `perfilResponsable`.

---

## ✅ Reglas de validación

**Insumo:** código de 3 a 20 caracteres (letras, números o guiones) y único · nombre de al menos 3 caracteres · categoría y unidad obligatorias · stock entre 0 y 1.000.000 · nivel mínimo entre 1 y 1.000.000 · ubicación obligatoria.

**Movimiento:** insumo obligatorio · cantidad entera mayor a 0 · en ventas y mermas la cantidad no puede superar el stock · motivo de al menos 3 caracteres · responsable obligatorio.

---

## 🧰 Tecnologías y dependencias

| Tecnología | Versión | Uso |
|---|---|---|
| Kotlin | 1.9.24 | Lenguaje principal |
| Android Gradle Plugin | 8.5.2 | Compilación |
| KSP | 1.9.24-1.0.20 | Generación de código de Room |
| Compose BOM | 2024.06.00 | Versiones compatibles de Compose |
| Material 3 + Window Size Class + Icons Extended | según BOM | Interfaz y adaptabilidad |
| Activity Compose | 1.9.1 | Launchers de cámara, galería y permisos |
| Lifecycle (runtime, runtime-compose, viewmodel-compose) | 2.8.4 | ViewModel y `StateFlow` |
| Navigation Compose | 2.7.7 | Navegación con transiciones |
| Room (runtime, ktx, compiler) | 2.6.1 | Persistencia local |
| Coil Compose | 2.6.0 | Carga de fotos |
| CameraX (core, camera2, lifecycle, view) | 1.3.4 | Cámara del escáner |
| ML Kit Barcode Scanning | 17.2.0 | Lectura de códigos de barras y QR |
| JUnit / AndroidX Test / Espresso | 4.13.2 / 1.2.1 / 3.6.1 | Pruebas |

Todas las versiones están centralizadas en `gradle/libs.versions.toml`.

---

## 📲 Recursos nativos del dispositivo

1. **Cámara y galería:** `TakePicture` / `GetContent` + `FileProvider`.
2. **Notificaciones locales:** canal de alta importancia con permiso `POST_NOTIFICATIONS` (Android 13+).
3. **Escáner de códigos:** CameraX + ML Kit, con permiso `CAMERA` solicitado en tiempo de ejecución.

**Configuración:** `minSdk 30` · `targetSdk 34` · `compileSdk 34` · Java 17.

---

## ▶️ Cómo ejecutar el proyecto

1. Instalar **Android Studio** (versión reciente) con **JDK 17**.
2. Abrir la carpeta `Appcappucare-master` y esperar la sincronización de Gradle.
3. Conectar un teléfono físico (recomendado para cámara y escáner) o iniciar un emulador con Android 11 o superior.
4. Presionar **Run ▶**.
5. Al iniciar, aceptar el permiso de notificaciones.

### Flujo de prueba sugerido
1. En **Inventario** → *Nuevo insumo*: registrar un producto con stock 6 y nivel mínimo 5.
2. En **Movimiento**: registrar una *Venta* de 2 unidades → el stock baja a 4 y se dispara la **notificación** de stock crítico.
3. Revisar el **Historial** y volver al **Inventario**, donde la tarjeta aparece en rojo con el banner de alerta.

---

## 🧪 Pruebas

```bash
./gradlew testDebugUnitTest          # validaciones de formularios
./gradlew connectedDebugAndroidTest  # prueba instrumentada (requiere dispositivo)
```

- `ValidacionesProductoTest` y `ValidacionesMovimientoTest`: reglas de negocio de ambos formularios.
- `PaqueteAplicacionTest`: verifica el paquete de la aplicación instalada.

---

## ⚠️ Limitaciones conocidas y mejoras futuras

- Los datos se guardan solo en el dispositivo (sin sincronización en la nube).
- No hay inicio de sesión ni permisos por rol; el responsable se elige de una lista.
- La base usa `fallbackToDestructiveMigration`: al subir la versión del esquema hay que definir migraciones.
- Posibles mejoras: reportes en PDF/Excel, fechas de vencimiento por lote, órdenes de compra automáticas y sincronización con el sistema de ventas.

---

<p align="center"><b>Cappucare</b> · Benjamín Rojas · Diego Sanzana<br/>DESARROLLO DE APLICACIONES MÓVILES_004D · DSY1105</p>