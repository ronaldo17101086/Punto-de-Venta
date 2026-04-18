package producto;

import javax.swing.ImageIcon;

public class ProductDTO {

    public String nombre;
    public String codigo;
    public double precio;
    public String descripcion;
    public String categoria;
    public String imagePath; // Ruta de la imagen

    public ProductDTO(String nombre, String codigo, double precio, String descripcion, String categoria) {
        this.nombre = nombre;
        this.codigo = codigo;
        this.precio = precio;
        this.descripcion = descripcion;
        this.categoria = categoria;
    }

    @Override
    public String toString() {
        return nombre; // Esto es lo que se verá en la lista
    }
}
