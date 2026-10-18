package installers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;

@Service
public class ProductTableService {

    @Autowired
    private DataSource dataSource;

    public void createTable() {
        // 1. Tabla de productos
        String sqlProductos = "CREATE TABLE IF NOT EXISTS productos (" 
                + "id INT AUTO_INCREMENT PRIMARY KEY, "
                + "sku VARCHAR(50) UNIQUE, "
                + "name VARCHAR(100) NOT NULL, "
                + "price DECIMAL(10, 2) NOT NULL, "
                + "image_path VARCHAR(255), "
                + "is_granel BOOLEAN DEFAULT FALSE"
                + ");";

        // 2. Tabla intermedia de ventas y productos
        String sqlVentaProductos = "CREATE TABLE IF NOT EXISTS venta_productos ("
                + "id INT AUTO_INCREMENT PRIMARY KEY, "
                + "registro_venta_id INT NOT NULL, "
                + "producto_id INT NOT NULL, "
                + "cantidad DECIMAL(10, 3) DEFAULT 1.000"
                + ");";

        // 3. Instrucción por si la tabla ya existía pero le faltaba la columna
        String sqlAlterVentaProductos = "ALTER TABLE venta_productos ADD COLUMN IF NOT EXISTS cantidad DECIMAL(10, 3) DEFAULT 1.000;";

        try (Connection conn = dataSource.getConnection(); Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sqlProductos);
            stmt.executeUpdate(sqlVentaProductos);
            
            // Intenta añadir la columna si la tabla previa no la tenía
            try {
                stmt.executeUpdate(sqlAlterVentaProductos);
            } catch (Exception ex) {
                // Si la columna ya existe, se ignora de forma segura
            }
            
            System.out.println("[DB-SERVICE] Tablas y estructura de cantidades configuradas correctamente.");
        } catch (Exception e) {
            System.err.println("[DB-SERVICE] Error al configurar las tablas por código: " + e.getMessage());
        }
    }
} 