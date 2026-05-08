package caja;

public enum TipoMovimiento {
    APERTURA, // Dinero base al abrir
    VENTA, // Ingreso por ventas (el más común)
    INGRESO_EXTRA, // Entradas ajenas a ventas
    RETIRO_GASTO, // Gastos operativos (luz, limpieza, papelería)
    PAGO_PROVEEDOR, // Salida directa para compras/mercancía
    DEVOLUCION, // Salida por devolución de cliente
    AJUSTE_DE_CAJA, // Correcciones por faltantes o sobrantes
    CIERRE              // Registro final del turno
}
