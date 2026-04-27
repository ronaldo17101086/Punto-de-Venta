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
        String sql = "CREATE TABLE IF NOT EXISTS productos (" 
                + "id INT AUTO_INCREMENT PRIMARY KEY, "
                + "sku VARCHAR(50) UNIQUE, "
                + "name VARCHAR(100) NOT NULL, "
                + "price DECIMAL(10, 2) NOT NULL, "
                + "image_path VARCHAR(255), "
                + "is_granel BOOLEAN DEFAULT FALSE"
                + ");";

        try (Connection conn = dataSource.getConnection(); Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
            System.out.println("[DB-SERVICE] Tabla productos lista.");
        } catch (Exception e) {
            System.err.println("[DB-SERVICE] Error: " + e.getMessage());
        }
    }
}
