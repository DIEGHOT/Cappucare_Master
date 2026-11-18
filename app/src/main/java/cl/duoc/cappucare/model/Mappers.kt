package cl.duoc.cappucare.model

import cl.duoc.cappucare.data.local.MovimientoEntity
import cl.duoc.cappucare.data.local.ProductoEntity
import cl.duoc.cappucare.data.remote.IndicadorDto
import cl.duoc.cappucare.data.remote.IndicadoresDto
import cl.duoc.cappucare.data.remote.MovimientoDto
import cl.duoc.cappucare.data.remote.MovimientoRequestDto
import cl.duoc.cappucare.data.remote.ProductoDto

/* ---------- Room <-> modelo ---------- */
fun ProductoEntity.toModel() = Producto(
    codigo, nombre, descripcion, categoria, stockActual, stockMinimo, unidadMedida, ubicacion, fotoUri
)

fun MovimientoEntity.toModel() = Movimiento(
    id = id,
    productoCodigo = productoCodigo,
    tipo = TipoMovimiento.desde(tipo),
    cantidad = cantidad,
    fecha = fecha,
    motivo = motivo,
    perfilResponsable = perfilResponsable,
    productoNombre = productoNombre,
    unidadMedida = unidadMedida,
    stockResultante = stockResultante
)

/* ---------- API (microservicios) <-> Room / modelo ---------- */
fun ProductoDto.toEntity() = ProductoEntity(
    codigo = codigo,
    nombre = nombre,
    descripcion = descripcion.orEmpty(),
    categoria = categoria,
    stockActual = stockActual,
    stockMinimo = stockMinimo,
    unidadMedida = unidadMedida,
    ubicacion = ubicacion,
    fotoUri = fotoUri
)

fun Producto.toDto() = ProductoDto(
    codigo, nombre, descripcion, categoria, stockActual, stockMinimo, unidadMedida, ubicacion, fotoUri
)

fun MovimientoDto.toEntity() = MovimientoEntity(
    id = id,
    productoCodigo = productoCodigo,
    productoNombre = productoNombre,
    unidadMedida = unidadMedida,
    tipo = tipo,
    cantidad = cantidad,
    stockResultante = stockResultante,
    fecha = fecha,
    motivo = motivo,
    perfilResponsable = perfilResponsable
)

fun Movimiento.toRequestDto() = MovimientoRequestDto(
    productoCodigo = productoCodigo,
    tipo = tipo.name,
    cantidad = cantidad,
    motivo = motivo,
    perfilResponsable = perfilResponsable
)

/* ---------- API externa -> modelo ---------- */
fun IndicadorDto.toModel() = Indicador(codigo, nombre, unidadMedida, fecha, valor)

/** Selecciona los indicadores que le interesan al negocio: UF, dólar y euro. */
fun IndicadoresDto.toModel(): List<Indicador> = listOfNotNull(uf, dolar, euro).map { it.toModel() }
