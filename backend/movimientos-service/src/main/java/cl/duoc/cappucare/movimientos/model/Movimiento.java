package cl.duoc.cappucare.movimientos.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Registro inmutable de un ingreso o salida de bodega. Guarda una copia del nombre y la unidad del
 * insumo para que el historial siga siendo legible aunque el insumo se elimine después.
 */
@Entity
@Table(name = "movimientos")
public class Movimiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String productoCodigo;

    @Column(nullable = false, length = 100)
    private String productoNombre;

    @Column(nullable = false, length = 20)
    private String unidadMedida;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoMovimiento tipo;

    @Column(nullable = false)
    private int cantidad;

    /** Saldo del insumo inmediatamente después del movimiento. */
    @Column(nullable = false)
    private int stockResultante;

    /** Fecha y hora en milisegundos desde 1970 (UTC). */
    @Column(nullable = false)
    private long fecha;

    @Column(nullable = false, length = 200)
    private String motivo;

    @Column(nullable = false, length = 30)
    private String perfilResponsable;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getProductoCodigo() { return productoCodigo; }
    public void setProductoCodigo(String productoCodigo) { this.productoCodigo = productoCodigo; }
    public String getProductoNombre() { return productoNombre; }
    public void setProductoNombre(String productoNombre) { this.productoNombre = productoNombre; }
    public String getUnidadMedida() { return unidadMedida; }
    public void setUnidadMedida(String unidadMedida) { this.unidadMedida = unidadMedida; }
    public TipoMovimiento getTipo() { return tipo; }
    public void setTipo(TipoMovimiento tipo) { this.tipo = tipo; }
    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }
    public int getStockResultante() { return stockResultante; }
    public void setStockResultante(int stockResultante) { this.stockResultante = stockResultante; }
    public long getFecha() { return fecha; }
    public void setFecha(long fecha) { this.fecha = fecha; }
    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
    public String getPerfilResponsable() { return perfilResponsable; }
    public void setPerfilResponsable(String perfilResponsable) { this.perfilResponsable = perfilResponsable; }
}
