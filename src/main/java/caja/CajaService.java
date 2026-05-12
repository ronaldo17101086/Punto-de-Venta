package caja;

import java.sql.*;
import java.util.Vector;
import javax.sql.DataSource;
import javax.swing.table.DefaultTableModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Servicio de Gestión de Caja - Persistencia en MySQL Utiliza el DataSource de
 * Spring para evitar errores de credenciales manuales.
 */
@Service
public class CajaService {

    @Autowired
    private DataSource dataSource;

    public CajaService() {
        // La inicialización se debe llamar después de que Spring inyecte el DataSource
        // Por eso usamos una validación en los métodos o un @PostConstruct
    }

    /**
     * Crea la tabla de movimientos si no existe.
     */
    public void inicializarInfraestructura() {
        String query = "CREATE TABLE IF NOT EXISTS caja_movimientos ("
                + "id INT PRIMARY KEY AUTO_INCREMENT, "
                + "id_caja INT, "
                + "tipo_movimiento VARCHAR(50), "
                + "monto DOUBLE, "
                + "comentario TEXT, "
                + "usuario VARCHAR(50), "
                + "fecha_hora TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";

        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute(query);
            System.out.println("[DB-SERVICE] Tabla caja_movimientos lista en MySQL.");
        } catch (SQLException e) {
            System.err.println("ERROR AL INICIALIZAR CAJA EN MYSQL: " + e.getMessage());
        }
    }

    public boolean sendRequest(String method, int idCaja, TipoMovimiento tipo, double monto, String comentario, Integer idMovimiento) {
        if ("POST".equalsIgnoreCase(method)) {
            // Aquí llamamos a postMovimiento pasando los datos necesarios
            return postMovimiento(idCaja, tipo, monto, comentario);
        }
        // Si en el futuro agregas "PUT" o "DELETE", usarías el idMovimiento aquí
        return false;
    }

    private boolean postMovimiento(int idCaja, TipoMovimiento tipo, double monto, String comentario) {
        double montoFinal = (tipo == TipoMovimiento.RETIRO_GASTO
                || tipo == TipoMovimiento.PAGO_PROVEEDOR
                || tipo == TipoMovimiento.DEVOLUCION) ? -Math.abs(monto) : Math.abs(monto);

        String sql = "INSERT INTO caja_movimientos (id_caja, tipo_movimiento, monto, comentario, usuario) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idCaja);
            pstmt.setString(2, tipo.name());
            pstmt.setDouble(3, montoFinal);
            pstmt.setString(4, comentario);
            pstmt.setString(5, "ADMIN");
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void fetchMovimientosATabla(DefaultTableModel modelo, int idCaja) {
        modelo.setRowCount(0);
        String sql = "SELECT * FROM caja_movimientos WHERE id_caja = ? ORDER BY id DESC";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idCaja);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Vector<Object> fila = new Vector<>();
                fila.add(rs.getInt("id"));
                fila.add(rs.getTimestamp("fecha_hora").toString());
                fila.add(rs.getString("tipo_movimiento"));
                fila.add(rs.getString("comentario"));
                fila.add(String.format("$%.2f", rs.getDouble("monto")));
                fila.add(rs.getString("usuario"));
                modelo.addRow(fila);
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener movimientos: " + e.getMessage());
        }
    }

    public double getSaldoTotal(int idCaja) {
        String sql = "SELECT SUM(monto) FROM caja_movimientos WHERE id_caja = ?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idCaja);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble(1);
            }
        } catch (SQLException e) {
            System.err.println("Error al calcular saldo: " + e.getMessage());
        }
        return 0.0;
    }

    /**
     * Obtiene la conexión directamente del pool de conexiones de Spring.
     */
    private Connection getConnection() throws SQLException {
        if (dataSource == null) {
            throw new SQLException("El DataSource no ha sido inyectado correctamente por Spring.");
        }
        return dataSource.getConnection();
    }
}
