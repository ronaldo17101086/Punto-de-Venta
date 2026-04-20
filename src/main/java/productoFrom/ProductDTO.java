package productoFrom;

import javax.swing.ImageIcon;

public class ProductDTO {

    public String nombre;
    public String codigo;
    public double precio;
    public String descripcion;
    public String categoria;
    public String imagePath; // Mantenemos este nombre

    // Actualizamos el constructor para que RECIBA la ruta
    public ProductDTO(String nombre, String codigo, double precio, String descripcion, String categoria, String imagePath) {
        this.nombre = nombre;
        this.codigo = codigo;
        this.precio = precio;
        this.descripcion = descripcion;
        this.categoria = categoria;
        this.imagePath = imagePath; // AHORA SÍ se guarda la ruta
    }

    @Override
    public String toString() {
        return nombre;
    }
}
