# 💊 Cappucare — Control inteligente y oportuno de existencias

> Aplicación móvil Android (Kotlin + Jetpack Compose) conectada a **microservicios Spring Boot** para controlar las existencias de insumos médicos de Cappucare y mantener la trazabilidad de sus ingresos y ventas.

---

## 1. Nombre del proyecto

**Cappucare** (`cl.duoc.cappucare`) · Evaluación Parcial 4 · Proyecto semestral

## 2. Integrantes

| Nombre | Rol |
|---|---|
| **Benjamín Rojas** | Desarrollador |
| **Diego Sanzana** | Desarrollador |

| | |
|---|---|
| **Asignatura** | DESARROLLO DE APLICACIONES MÓVILES_004D (DSY1105) |
| **Caso** | Control inteligente y oportuno de existencias — Cappucare (venta de productos e insumos médicos) |

---

## 📌 Contexto del negocio

Cappucare vende productos e insumos médicos. La información de sus existencias puede quedar dispersa o desactualizada, lo que produce diferencias entre lo registrado y lo disponible, omisiones en entradas o ventas y falta de stock de insumos que se agotan sin aviso.

| Necesidad del caso | Cómo la resuelve la aplicación |
|---|---|
| Conocer la disponibilidad real | Inventario sincronizado con el servidor, con ubicación en bodega, estado y foto de cada insumo |
| Reducir diferencias entre lo registrado y lo disponible | Cada ingreso, venta o merma ajusta el stock en el servidor y deja un registro inmutable |
| Historial comprensible de entradas y salidas | Historial con tipo, cantidad, motivo, responsable, fecha y saldo resultante |
| Identificar a tiempo qué hay que comprar | Alertas visuales y **notificación del sistema** cuando el stock llega al nivel mínimo |
| Facilitar el registro de ventas | Formulario de movimiento que valida que no se venda más de lo que hay |
| Identificar productos con cámara | Escáner de códigos de barras / QR y foto del insumo |
| Apoyar decisiones de compra | Indicadores económicos (dólar, euro, UF) desde una API externa |

---

## 3. Funcionalidades

- **Inventario:** listado en tarjetas, búsqueda por nombre/código/categoría, filtro de stock crítico, banner de alertas, sincronización manual, crear, editar y eliminar insumos.
- **Formulario de insumo:** código (escaneable), nombre, descripción, categoría, stock actual, nivel mínimo, unidad de medida, ubicación en bodega y foto (cámara o galería). El **estado** del producto (Disponible / Stock bajo / Agotado) se calcula según sus existencias.
- **Movimientos:** ingreso por recepción, salida por venta y salida por merma, con perfil responsable (Bodeguero, Vendedor, Administrador) y fecha automática.
- **Historial:** trazabilidad completa; se conserva aunque el insumo se elimine.
- **Mercado (API externa):** valor del dólar, euro y UF como referencia para estimar el costo de reposición de insumos importados.
- **Notificaciones locales** de stock crítico.
- **Modo sin conexión:** la app muestra los últimos datos guardados en el dispositivo (Room) y avisa si no puede conectar con los microservicios.
- **Diseño adaptable:** barra inferior (teléfono), drawer modal (tablet pequeña) y drawer permanente (tablet grande).
- **Animaciones:** transiciones entre pantallas, `Crossfade`, `AnimatedVisibility`, botón con rebote y progreso, alertas pulsantes.
- **Validaciones** 100 % en el ViewModel (campos vacíos, números positivos, stock suficiente) y también en el servidor.

---

## 🏗️ Arquitectura

```
                       ┌─────────────────────────────┐
  App Android          │  productos-service  :8081   │──► H2 (productosdb)
  Compose + MVVM  ───► │  CRUD de insumos            │
  Retrofit             └──────────────▲──────────────┘
  Room (caché)                        │ ajusta stock
        │              ┌──────────────┴──────────────┐
        └───────────►  │  movimientos-service :8082  │──► H2 (movimientosdb)
        │              │  ingresos / ventas / mermas │
        │              └─────────────────────────────┘
        └───────────►  API externa  https://mindicador.cl/api
```

- **Fuente de verdad:** los microservicios. Room guarda una copia local: la UI siempre lee de Room, y toda escritura va primero al servidor.
- **Consistencia:** `movimientos-service` primero ajusta el stock en `productos-service` (que rechaza el movimiento con `409` si dejaría el stock negativo) y luego guarda el movimiento; si el guardado falla, revierte el ajuste.
- **Historial inmutable:** los movimientos no se editan ni se borran (trazabilidad); solo se crean y se consultan.

### Estructura del repositorio

```
Appcappucare-master/
├── app/                         # App Android
│   └── src/main/java/cl/duoc/cappucare/
│       ├── model/  data/local/  data/remote/  repository/  viewmodel/
│       ├── navigation/  notification/  ui/{screens,components,utils,theme}/
│   └── src/test/                # Pruebas unitarias (JUnit + MockK)
├── backend/
│   ├── productos-service/       # Spring Boot · puerto 8081
│   └── movimientos-service/     # Spring Boot · puerto 8082
├── keystore/cappucare-release.jks   # Llave de firma
├── keystore.properties              # Configuración de firma
├── release/                         # APK firmado (se genera con assembleRelease)
└── docs/                            # Trello, GitHub y checklist de la pauta
```

---

## 4. Endpoints utilizados

### 4.1 Microservicio `productos-service` — `http://<host>:8081`

| Método | Ruta | Descripción | Respuestas |
|---|---|---|---|
| GET | `/api/productos` | Lista los insumos (orden por nombre) | 200 |
| GET | `/api/productos/{codigo}` | Obtiene un insumo | 200 · 404 |
| POST | `/api/productos` | Crea un insumo | 201 · 400 · 409 (código repetido) |
| PUT | `/api/productos/{codigo}` | Actualiza un insumo | 200 · 400 · 404 |
| DELETE | `/api/productos/{codigo}` | Elimina un insumo | 204 · 404 |
| POST | `/api/productos/{codigo}/ajustar-stock` | Suma o resta unidades (`{"delta": -3}`) | 200 · 404 · 409 (stock insuficiente) |

Ejemplo de cuerpo (POST/PUT):

```json
{
  "codigo": "INS-009",
  "nombre": "Guantes de nitrilo talla L",
  "descripcion": "Caja de 100 guantes sin polvo",
  "categoria": "Equipos de protección",
  "stockActual": 40,
  "stockMinimo": 15,
  "unidadMedida": "Caja",
  "ubicacion": "Pasillo 1 - Estante A",
  "fotoUri": null
}
```

### 4.2 Microservicio `movimientos-service` — `http://<host>:8082`

| Método | Ruta | Descripción | Respuestas |
|---|---|---|---|
| GET | `/api/movimientos` | Historial (más recientes primero). Filtro opcional `?productoCodigo=INS-001` | 200 |
| GET | `/api/movimientos/{id}` | Obtiene un movimiento | 200 · 404 |
| POST | `/api/movimientos` | Registra un movimiento y ajusta el stock | 201 · 400 · 404 · 409 · 503 |

```json
{
  "productoCodigo": "INS-001",
  "tipo": "SALIDA_VENTA",
  "cantidad": 3,
  "motivo": "Venta factura 123",
  "perfilResponsable": "Vendedor"
}
```

`tipo`: `INGRESO`, `SALIDA_VENTA` o `SALIDA_MERMA`. Los errores se devuelven como `{"mensaje": "..."}`.

### 4.3 API externa (Retrofit) — mindicador.cl

| Método | URL | Uso |
|---|---|---|
| GET | `https://mindicador.cl/api` | Valor actual de la **UF**, el **dólar observado** y el **euro**; se muestran en la pestaña *Mercado* |

Es una API pública y gratuita (sin clave). Se consume con Retrofit y no interfiere con los datos locales ni con los microservicios: si falla, solo la pestaña *Mercado* muestra el error.

---

## 5. Pasos para ejecutar

### Requisitos
- **JDK 17** y **Maven 3.9+** (IntelliJ IDEA los incluye) para los microservicios.
- **Android Studio** Koala (2024.1.1) o superior para la app.
- Conexión a internet (descarga de dependencias y API externa).

### Paso 1 — Levantar los microservicios

Abrir **dos terminales**, una por servicio, y **primero** iniciar productos:

```bash
cd backend/productos-service
mvn spring-boot:run          # http://localhost:8081  (carga un catálogo ficticio de 8 insumos)
```

```bash
cd backend/movimientos-service
mvn spring-boot:run          # http://localhost:8082
```

Comprobar: abrir <http://localhost:8081/api/productos> en el navegador. La base de datos H2 queda en `backend/*/data/` (persiste entre reinicios) y se puede revisar en <http://localhost:8081/h2-console> (JDBC URL `jdbc:h2:file:./data/productosdb`, usuario `sa`, sin contraseña).
Opcional: MySQL con `--spring.profiles.active=mysql` (ver `application-mysql.properties`).

### Paso 2 — Configurar la dirección del servidor en la app

En `gradle.properties`:

```properties
# Emulador de Android Studio (por defecto)
cappucare.backendHost=10.0.2.2
# Teléfono físico: IP del PC en la misma red Wi-Fi, por ejemplo
# cappucare.backendHost=192.168.1.50
```

Con teléfono físico, permitir los puertos 8081 y 8082 en el firewall del PC.

### Paso 3 — Ejecutar la app

1. Abrir la carpeta `Appcappucare-master` en Android Studio (JDK 17) y esperar el *Sync* de Gradle.
2. Ejecutar en un emulador o teléfono con Android 11 o superior.
3. Aceptar el permiso de notificaciones.

### Flujo de prueba sugerido
1. **Inventario:** deberían aparecer los 8 insumos del catálogo; algunos en rojo (stock crítico).
2. **Nuevo insumo:** crear uno con foto, y verificarlo en <http://localhost:8081/api/productos>.
3. **Movimiento:** vender más unidades de las disponibles → la app muestra el rechazo; luego vender una cantidad válida hasta llegar al mínimo → aparece la **notificación**.
4. **Historial:** revisar el movimiento con su saldo resultante.
5. **Mercado:** ver los indicadores de la API externa.
6. Detener los microservicios y tocar *Sincronizar*: la app avisa y conserva los datos locales.

### Pruebas unitarias y cobertura

```bash
# App (JUnit + MockK + coroutines-test)
./gradlew testDebugUnitTest
./gradlew createDebugUnitTestCoverageReport
# Informe: app/build/reports/coverage/test/debug/index.html  (revisar los paquetes viewmodel, repository y model)

# Microservicios (JUnit + Mockito)
cd backend/productos-service  && mvn test
cd backend/movimientos-service && mvn test
```

Las pruebas de la app cubren los tres ViewModels, el repositorio (éxito, conflictos 409, 404, errores del servidor y falta de conexión), el manejo de errores, los mapeos, `DataState` y las validaciones de ambos formularios.

---

## 6. APK firmado y llave `.jks`

| Elemento | Ubicación / valor |
|---|---|
| Llave de firma | `keystore/cappucare-release.jks` |
| Configuración | `keystore.properties` + bloque `signingConfigs` en `app/build.gradle.kts` |
| Alias | `cappucare` |
| Algoritmo | RSA 2048 bits · validez 10.000 días |
| Huella SHA-256 del certificado | `1C:68:B8:F4:E9:48:B9:C9:AD:20:ED:15:78:5B:2F:97:1F:62:D6:5D:9E:98:37:BB:7E:58:5E:2A:FA:BC:E0:23` |
| APK firmado | `release/Cappucare-v2.0-release.apk` |

> Las contraseñas están en `keystore.properties` solo porque la pauta exige entregar la llave junto a su configuración. Es una llave **académica**; en un proyecto real no se publica.

### Generar el APK firmado

```bash
./gradlew assembleRelease
# Resultado: app/build/outputs/apk/release/app-release.apk
cp app/build/outputs/apk/release/app-release.apk release/Cappucare-v2.0-release.apk
```

En Android Studio también: *Build → Generate Signed App Bundle / APK → APK*, usando `keystore/cappucare-release.jks`.

### Verificar la firma

```bash
keytool -list -v -keystore keystore/cappucare-release.jks -storepass cappucare2026
apksigner verify --print-certs release/Cappucare-v2.0-release.apk
```

(`apksigner` está en `<Android SDK>/build-tools/<versión>/`.)

---

## 🧰 Tecnologías

| App Android | Backend |
|---|---|
| Kotlin 1.9.24 · Jetpack Compose (BOM 2024.06) · Material 3 | Java 17 · Spring Boot 3.3.5 |
| MVVM · StateFlow · Navigation Compose 2.7.7 | Spring Web · Spring Data JPA · Bean Validation |
| Retrofit 2.11 · OkHttp 4.12 · Gson | H2 (archivo) · MySQL opcional |
| Room 2.6.1 (caché) · Coil · CameraX · ML Kit | JUnit 5 · Mockito |
| JUnit 4 · MockK 1.13 · coroutines-test | |

## 👥 Trabajo en equipo

El plan de tareas y el flujo de trabajo en GitHub y Trello están en [`docs/TRELLO_Y_GITHUB.md`](docs/TRELLO_Y_GITHUB.md). La correspondencia con la pauta de evaluación está en [`docs/CHECKLIST_PAUTA.md`](docs/CHECKLIST_PAUTA.md).

---

<p align="center"><b>Cappucare</b> · Benjamín Rojas · Diego Sanzana<br/>DESARROLLO DE APLICACIONES MÓVILES_004D · DSY1105</p>
