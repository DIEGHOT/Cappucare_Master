# Checklist de la Evaluación Parcial 4

| Indicador | Qué se pide | Evidencia en el proyecto | Pendiente del equipo |
|---|---|---|---|
| IE 3.1.1 (15 %) | App con todas sus pantallas, formularios y flujos sin errores | Inventario, formulario de insumo, movimiento, historial y mercado; navegación adaptable | Probar el recorrido completo en emulador y teléfono |
| IE 3.1.2 (15 %) | Microservicios Spring Boot con base de datos y endpoints | `backend/productos-service` y `backend/movimientos-service` (H2, 6 + 3 endpoints) | Levantarlos y probar con el navegador |
| IE 3.1.3 (15 %) | App integrada con los microservicios (CRUD) | Retrofit + repositorio: crear, leer, actualizar y eliminar insumos; registrar y consultar movimientos | Demostrar el CRUD en la defensa |
| IE 3.1.4 (15 %) | API externa con Retrofit mostrada en la interfaz | Pestaña *Mercado* con mindicador.cl | Verificar que el servicio responda el día de la entrega |
| IE 3.2.1 (15 %) | Pruebas unitarias ≥ 80 % de la lógica (JUnit, MockK) | `app/src/test/...` (ViewModels, repositorio, errores, mapeos, validaciones) | Ejecutar `createDebugUnitTestCoverageReport` y revisar el porcentaje |
| IE 3.3.1 (10 %) | APK firmado con .jks y configuración | `keystore/`, `keystore.properties`, `signingConfigs` en `app/build.gradle.kts` | Ejecutar `assembleRelease`, copiar el APK a `release/` y subirlo |
| IE 3.3.2 (15 %) | GitHub y Trello con actividad real y commits por integrante | Guía en `docs/TRELLO_Y_GITHUB.md` | Crear repositorio y tablero, repartir tareas y hacer commits ambos |

## Entregables

- [ ] Repositorio GitHub público con acceso al docente
- [ ] `README.md` (nombre, integrantes, funcionalidades, endpoints, pasos para ejecutar, captura del APK firmado y .jks)
- [ ] Capturas en `docs/capturas/` (APK firmado, .jks, Trello)
- [ ] Código de la app con commits de ambos integrantes
- [ ] Código de los microservicios con commits de ambos integrantes
- [ ] APK firmado en `release/` y llave `.jks`
- [ ] Código subido a AVA (grupal) y enlace individual activado
