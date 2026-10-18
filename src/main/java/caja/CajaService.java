package caja;

import java.sql.*;
import java.util.Vector;
import javax.sql.DataSource;
import javax.swing.table.DefaultTableModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CajaService {

    @Autowired
    private DataSource dataSource;

    public CajaService() {
    }

    @Autowired
    public void inicializarInfraestructura() {
        String sqlCaja = "CREATE TABLE IF NOT EXISTS caja ("
                + "id_caja INT AUTO_INCREMENT PRIMARY KEY, "
                + "nombre_caja VARCHAR(100) NOT NULL)";

        String sqlMovimientos = "CREATE TABLE IF NOT EXISTS caja_movimientos ("
                + "id INT PRIMARY KEY AUTO_INCREMENT, "
                + "id_caja INT, "
                + "tipo_movimiento VARCHAR(50), "
                + "monto DOUBLE, "
                + "concepto TEXT, "
                + "referencia VARCHAR(100), "
                + "proveedor_id VARCHAR(50), "
                + "nombre_proveedor VARCHAR(150), "
                + "usuario VARCHAR(50), "
                + "fecha_hora TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
                + "metodo_pago VARCHAR(50), "
                + "observaciones TEXT)";

        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute(sqlCaja);
            stmt.execute(sqlMovimientos);
            stmt.executeUpdate("INSERT IGNORE INTO caja (id_caja, nombre_caja) VALUES (1, 'Terminal: 01-PRINCIPAL')");
            System.out.println("[DB-SERVICE] Infraestructura de Caja verificada correctamente.");
        } catch (SQLException e) {
            System.err.println("ERROR AL CREAR LAS TABLAS DE CAJA: " + e.getMessage());
        }
    }

    public String getNombreCaja(int idCaja) {
        String sql = "SELECT nombre_caja FROM caja WHERE id_caja = ?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idCaja);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("nombre_caja");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "Terminal: 01-PRINCIPAL";
    }

    public boolean registrarMovimiento(int idCaja, MovimientoCajaDto mov, double efectivoRecibido, boolean esCredito) {
        if (esCredito && mov.getTipo() == TipoMovimiento.VENTA) {
            System.out.println("[CAJA] Venta a crédito detectada. La caja no se ve afectada.");
            return true; 
        }

        double montoFinal = mov.getMonto();
        
        if (mov.getTipo() == TipoMovimiento.RETIRO_GASTO
                || mov.getTipo() == TipoMovimiento.PAGO_PROVEEDOR
                || mov.getTipo() == TipoMovimiento.DEVOLUCION) {
            montoFinal = -Math.abs(mov.getMonto());
        } else if (mov.getTipo() == TipoMovimiento.VENTA 
                || mov.getTipo() == TipoMovimiento.APERTURA 
                || mov.getTipo() == TipoMovimiento.INGRESO_EXTRA 
                || mov.getTipo() == TipoMovimiento.AJUSTE_DE_CAJA) {
            montoFinal = mov.getMonto(); 
        }

        if (mov.getTipo() == TipoMovimiento.VENTA && efectivoRecibido > 0) {
            double cambio = efectivoRecibido - mov.getMonto();
            if (cambio < 0) cambio = 0;
            String obsActual = mov.getObservaciones() != null ? mov.getObservaciones() : "";
            mov.setObservaciones(String.format("Recibido: $%.2f | Cambio devuelto: $%.2f. %s", 
                    efectivoRecibido, cambio, obsActual));
        }

        String sql = "INSERT INTO caja_movimientos (id_caja, tipo_movimiento, monto, concepto, referencia, proveedor_id, nombre_proveedor, usuario, metodo_pago, observaciones) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idCaja);
            pstmt.setString(2, mov.getTipo() != null ? mov.getTipo().name() : "VENTA");
            pstmt.setDouble(3, montoFinal);
            pstmt.setString(4, mov.getConcepto() != null ? mov.getConcepto() : "Sin concepto");
            pstmt.setString(5, mov.getReferencia());
            pstmt.setString(6, mov.getProveedorId());
            pstmt.setString(7, mov.getNombreProveedor());
            pstmt.setString(8, mov.getUsuario() != null ? mov.getUsuario() : "ADMIN");
            pstmt.setString(9, mov.getMetodoPago() != null ? mov.getMetodoPago().name() : "EFECTIVO");
            pstmt.setString(10, mov.getObservaciones());
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean sendRequest(String method, int idCaja, TipoMovimiento tipo, double monto, String comentario, Integer idMovimiento) {
        if ("POST".equalsIgnoreCase(method)) {
            MovimientoCajaDto mov = new MovimientoCajaDto();
            mov.setTipo(tipo);
            mov.setMonto(monto);
            mov.setConcepto(comentario);
            mov.setUsuario("ADMIN");
            return registrarMovimiento(idCaja, mov, 0.0, false);
        }
        return false;
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
                
                String detalleComentario = rs.getString("concepto");
                String obs = rs.getString("observaciones");
                if (obs != null && !obs.isEmpty()) {
                    detalleComentario += " | " + obs;
                }
                fila.add(detalleComentario);
                fila.add(String.format("$%.2f", rs.getDouble("monto")));
                fila.add(rs.getString("usuario"));
                modelo.addRow(fila);
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener movimientos: " + e.getMessage());
        }
    }

    /**
     * Obtiene el ID del último punto de corte (CIERRE). 
     * Si existe un cierre posterior a la apertura, los cálculos empiezan limpios desde ese corte.
     */
    private int getIdUltimoCorteOrApertura(Connection conn, int idCaja) throws SQLException {
        String sql = "SELECT id, tipo_movimiento FROM caja_movimientos WHERE id_caja = ? AND tipo_movimiento IN ('APERTURA', 'CIERRE') ORDER BY id DESC LIMIT 1";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idCaja);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String tipo = rs.getString("tipo_movimiento");
                if ("CIERRE".equalsIgnoreCase(tipo)) {
                    // Si el último fue un cierre, el límite es exactamente ese ID (todo lo posterior al cierre)
                    return rs.getInt("id");
                } else {
                    // Si el último fue una apertura, tomamos desde esa apertura
                    return rs.getInt("id");
                }
            }
        }
        return 0;
    }

    public double getSaldoTotal(int idCaja) {
        try (Connection conn = getConnection()) {
            int idCorteOrApertura = getIdUltimoCorteOrApertura(conn, idCaja);
            // Si el último movimiento fue un cierre exacto, el saldo empieza en 0
            String sqlVerifica = "SELECT tipo_movimiento FROM caja_movimientos WHERE id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sqlVerifica)) {
                pstmt.setInt(1, idCorteOrApertura);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next() && "CIERRE".equalsIgnoreCase(rs.getString("tipo_movimiento"))) {
                    // Si está cerrado, sumamos solo los movimientos estrictamente posteriores al cierre
                    String sqlPost = "SELECT COALESCE(SUM(monto), 0.0) FROM caja_movimientos WHERE id_caja = ? AND id > ?";
                    try (PreparedStatement p2 = conn.prepareStatement(sqlPost)) {
                        p2.setInt(1, idCaja);
                        p2.setInt(2, idCorteOrApertura);
                        ResultSet r2 = p2.executeQuery();
                        if (r2.next()) return r2.getDouble(1);
                    }
                    return 0.0;
                }
            }

            // Si fue apertura normal, sumamos desde la apertura
            String sql = "SELECT COALESCE(SUM(monto), 0.0) FROM caja_movimientos WHERE id_caja = ? AND id >= ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, idCaja);
                pstmt.setInt(2, idCorteOrApertura);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    return rs.getDouble(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al calcular saldo: " + e.getMessage());
        }
        return 0.0;
    }

    public double getTotalVentasDia(int idCaja) {
        try (Connection conn = getConnection()) {
            int idCorteOrApertura = getIdUltimoCorteOrApertura(conn, idCaja);
            String sql = "SELECT COALESCE(SUM(monto), 0.0) FROM caja_movimientos WHERE id_caja = ? AND tipo_movimiento = 'VENTA' AND id > ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, idCaja);
                pstmt.setInt(2, idCorteOrApertura);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    return rs.getDouble(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    public double getTotalGastosSalidas(int idCaja) {
        try (Connection conn = getConnection()) {
            int idCorteOrApertura = getIdUltimoCorteOrApertura(conn, idCaja);
            String sql = "SELECT COALESCE(SUM(ABS(monto)), 0.0) FROM caja_movimientos WHERE id_caja = ? AND tipo_movimiento IN ('RETIRO_GASTO', 'PAGO_PROVEEDOR', 'DEVOLUCION') AND id > ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, idCaja);
                pstmt.setInt(2, idCorteOrApertura);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    return rs.getDouble(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    public double getMontoApertura(int idCaja) {
        try (Connection conn = getConnection()) {
            String sql = "SELECT tipo_movimiento, monto FROM caja_movimientos WHERE id_caja = ? AND tipo_movimiento IN ('APERTURA', 'CIERRE') ORDER BY id DESC LIMIT 1";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, idCaja);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    String tipo = rs.getString("tipo_movimiento");
                    // Si el último movimiento fue un cierre, el monto de apertura se queda en 0.0 hasta que abran de nuevo
                    if ("CIERRE".equalsIgnoreCase(tipo)) {
                        return 0.0;
                    } else {
                        return rs.getDouble("monto");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    private Connection getConnection() throws SQLException {
        if (dataSource == null) {
            throw new SQLException("El DataSource no ha sido inyectado correctamente por Spring.");
        }
        return dataSource.getConnection();
    }
}