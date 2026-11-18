package cl.duoc.cappucare.productos.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Insumo médico comercializado por Cappucare. */
@Entity
@Table(name = "productos")
public class Producto {

    @Id
    @Column(length = 20)
    private String codigo;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(length = 200)
    private String descripcion;

    @Column(nullable = false, length = 60)
    private String categoria;

    @Column(nullable = false)
    private int stockActual;

    @Column(nullable = false)
    private int stockMinimo;

    @Column(nullable = false, length = 20)
    private String unidadMedida;

    @Column(nullable = false, length = 100)
    private String ubicacion;

    @Column(length = 500)
    private String fotoUri;

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
    public int getStockActual() { return stockActual; }
    public void setStockActual(int stockActual) { this.stockActual = stockActual; }
    public int getStockMinimo() { return stockMinimo; }
    public void setStockMinimo(int stockMinimo) { this.stockMinimo = stockMinimo; }
    public String getUnidadMedida() { return unidadMedida; }
    public void setUnidadMedida(String unidadMedida) { this.unidadMedida = unidadMedida; }
    public String getUbicacion() { return ubicacion; }
    public void setUbicacion(String ubicacion) { this.ubicacion = ubicacion; }
    public String getFotoUri() { return fotoUri; }
    public void setFotoUri(String fotoUri) { this.fotoUri = fotoUri; }
}
