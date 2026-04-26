package tools;

public enum FormaPagoDto {
    EFECTIVO("Efectivo"),
    TARJETA("Tarjeta"),
    TRANSFERENCIA("Transferencia"),
    QR("QR / App"),
    VALES("Vales"),
    CREDITO("Crédito / Fiao"),
    CHEQUE("Cheque"),
    MIXTO("Pago Mixto");

    private final String nombre;

    FormaPagoDto(String nombre) {
        this.nombre = nombre;
    }

    @Override
    public String toString() {
        return nombre;
    }
    
    public String getNombre() {
        return nombre;
    }
}