package cl.duoc.cappucare.model

import cl.duoc.cappucare.data.local.MovimientoEntity
import cl.duoc.cappucare.data.local.ProductoEntity

fun ProductoEntity.toModel() = Producto(
    codigo, nombre, categoria, stockActual, stockMinimo, unidadMedida, ubicacion, fotoUri
)

fun Producto.toEntity() = ProductoEntity(
    codigo, nombre, categoria, stockActual, stockMinimo, unidadMedida, ubicacion, fotoUri
)

fun MovimientoEntity.toModel() = Movimiento(
    id, productoCodigo, TipoMovimiento.desde(tipo), cantidad, fecha, motivo, perfilResponsable
)

fun Movimiento.toEntity() = MovimientoEntity(
    id, productoCodigo, tipo.name, cantidad, fecha, motivo, perfilResponsable
)
