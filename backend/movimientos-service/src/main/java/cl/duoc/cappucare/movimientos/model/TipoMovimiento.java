package cl.duoc.cappucare.movimientos.model;

public enum TipoMovimiento {
    INGRESO(false),
    SALIDA_VENTA(true),
    SALIDA_MERMA(true);

    private final boolean salida;

    TipoMovimiento(boolean salida) {
        this.salida = salida;
    }

    public boolean esSalida() {
        return salida;
    }
}
