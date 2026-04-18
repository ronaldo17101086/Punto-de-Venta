/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package tools;

/**
 *
 * @author ronal
 */
public enum FormaPago {
    EFECTIVO("💵 Efectivo"),
    TARJETA("💳 Tarjeta"),
    TRANSFERENCIA("📲 Transferencia"),
    VALES("🎫 Vales"),
    CREDITO("🏦 Crédito"),
    QR("📱 QR"),
    MIXTO("🔀 Mixto");

    private final String nombre;

    FormaPago(String nombre) {
        this.nombre = nombre;
    }

    public String getNombre() {
        return nombre;
    }
}
