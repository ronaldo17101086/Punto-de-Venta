
package tools;

public enum FormaPagoDto {
    EFECTIVO("💵 Efectivo"),
    TARJETA("💳 Tarjeta"),
    TRANSFERENCIA("📲 Transferencia"),
    VALES("🎫 Vales"),
    CREDITO("🏦 Crédito"),
    QR("📱 QR"),
    MIXTO("🔀 Mixto");

    private final String nombre;

    FormaPagoDto(String nombre) {
        this.nombre = nombre;
    }

    public String getNombre() {
        return nombre;
    }
}
