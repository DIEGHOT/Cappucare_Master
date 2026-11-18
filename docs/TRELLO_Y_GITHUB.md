# Trabajo en equipo: Trello y GitHub

La pauta (IE 3.3.2) evalúa **actividad real**: tareas distribuidas, commits por integrante y colaboración. Este documento propone cómo organizarla; los commits y las tarjetas deben ser hechos por ustedes.

## Tablero de Trello

Crear un tablero **Cappucare – DSY1105** (público o compartido con el docente) con estas listas:

`Backlog` → `En progreso` → `En revisión` → `Hecho`

### Tarjetas sugeridas (con responsable)

| Tarjeta | Responsable sugerido |
|---|---|
| Microservicio productos: entidad, repositorio, servicio, controlador y validaciones | Benjamín |
| Microservicio productos: carga inicial del catálogo y pruebas | Benjamín |
| Microservicio movimientos: entidad, servicio y cliente hacia productos | Diego |
| Microservicio movimientos: controlador, manejo de errores y pruebas | Diego |
| App: capa remota Retrofit (DTOs, APIs, ApiFactory) | Benjamín |
| App: repositorio con caché Room y sincronización | Diego |
| App: formularios y ViewModels (insumo y movimiento) | Benjamín |
| App: pantalla Mercado (API externa mindicador.cl) | Diego |
| Pruebas unitarias de ViewModels y repositorio (meta ≥ 80 %) | Ambos |
| Firma del APK (.jks, signingConfigs) y verificación | Diego |
| README, capturas y checklist de entrega | Benjamín |
| Prueba integral con emulador y teléfono físico | Ambos |

Cada tarjeta debe tener fecha límite, checklist de subtareas y el enlace al commit o *pull request* que la cierra. Tomen una captura del tablero y guárdenla en `docs/capturas/trello.png`.

## Flujo de GitHub

1. Crear un repositorio **público** (por ejemplo `cappucare-dsy1105`) y agregar al docente como colaborador (*Settings → Collaborators*).
2. Cada integrante configura su identidad para que los commits queden a su nombre:
   ```bash
   git config user.name  "Benjamín Rojas"      # o "Diego Sanzana"
   git config user.email "su-correo@..."
   ```
3. Ramas: `main` (estable) y una rama por tarea, por ejemplo `feature/productos-service`, `feature/repositorio-room`, `feature/pantalla-mercado`.
4. Commits pequeños y descriptivos, con prefijo: `feat:`, `fix:`, `test:`, `docs:`, `chore:`. Ejemplos:
   - `feat(productos): agrega endpoint para ajustar stock`
   - `test(app): prueba el rechazo de ventas sin stock`
5. Abrir un *pull request* por rama; el otro integrante lo revisa antes de fusionar.
6. Los dos repositorios exigidos (app y microservicios) pueden ir en el mismo repositorio, en las carpetas `app/` y `backend/`.
7. Antes de la fecha límite: etiquetar la entrega (`git tag v2.0-evaluacion-parcial-4`) y **no hacer más cambios**: la pauta asigna nota 1.0 si el repositorio cambia después del plazo.

## Subida a AVA

Un solo integrante sube el código fuente a AVA (los mismos archivos del repositorio, con el APK y la llave). Cada integrante activa además su enlace individual indicando nombre, apellido, número de equipo y nombre de la aplicación (**Cappucare**).
