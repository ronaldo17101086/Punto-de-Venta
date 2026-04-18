package Conexion;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

@Configuration // <--- Esto le dice a Spring que use esta configuración
public class Conexion {

    private static final String URL_SERVIDOR = "jdbc:mysql://localhost:3306/?serverTimezone=UTC";
    private static final String URL_DB = "jdbc:mysql://localhost:3306/chancuellar?serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASSWORD = "root";

    @Bean // <--- Esto soluciona el error "Failed to determine a suitable driver class"
    public DataSource dataSource() {
        crearBaseDeDatosSiNoExiste();
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        dataSource.setUrl(URL_DB);
        dataSource.setUsername(USER);
        dataSource.setPassword(PASSWORD);
        return dataSource;
    }

    public static Connection conectar() {
        try {
            crearBaseDeDatosSiNoExiste();
            return DriverManager.getConnection(URL_DB, USER, PASSWORD);
        } catch (SQLException e) {
            System.err.println("Error al conectar: " + e.getMessage());
            return null;
        }
    }

    private static void crearBaseDeDatosSiNoExiste() {
        try (Connection tempCon = DriverManager.getConnection(URL_SERVIDOR, USER, PASSWORD); Statement stmt = tempCon.createStatement()) {
            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS chancuellar");
        } catch (SQLException e) {
            System.err.println("No se pudo verificar/crear la base de datos: " + e.getMessage());
        }
    }
}
