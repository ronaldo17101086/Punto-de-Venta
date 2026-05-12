package installers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class CajaTableService {

    // Puedes usar SQLite o H2 para no depender de una instalación externa de SQL
    private static final String URL = "jdbc:h2:./data/pos_db;AUTO_SERVER=TRUE";

    public static void inicializarTablas() {
        try (Connection conn = DriverManager.getConnection(URL, "sa", ""); Statement stmt = conn.createStatement()) {

            // Crear tabla de Cajas (para n tipos de cajas)
            stmt.execute("CREATE TABLE IF NOT EXISTS cajas ("
                    + "id INT PRIMARY KEY AUTO_INCREMENT, "
                    + "nombre VARCHAR(100) NOT NULL, "
                    + "estado VARCHAR(20) DEFAULT 'ABIERTA')");

            // Crear tabla de Movimientos con soporte para números reales y comentarios
            stmt.execute("CREATE TABLE IF NOT EXISTS caja_movimientos ("
                    + "id INT PRIMARY KEY AUTO_INCREMENT, "
                    + "id_caja INT, "
                    + "tipo_movimiento VARCHAR(50), "
                    + "monto DOUBLE, "
                    + // Números reales
                    "comentario TEXT, "
                    + "usuario VARCHAR(50), "
                    + "fecha_hora TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

            System.out.println("Base de datos e inicialización completa en Java.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
