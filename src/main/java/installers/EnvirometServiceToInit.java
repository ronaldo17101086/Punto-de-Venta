package installers;

import caja.CajaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.io.File;

@Component
public class EnvirometServiceToInit {

    private final ProductTableService productTableService;
    private final CajaService cajaService; // Añadido

    // El constructor ahora recibe ambos. Spring se encarga de inyectarlos.
    @Autowired
    public EnvirometServiceToInit(ProductTableService productTableService, CajaService cajaService) {
        this.productTableService = productTableService;
        this.cajaService = cajaService;
    }

    public void runSetup() {
        System.out.println("--- Starting System Check ---");
        try {
            verifyAndInstall("C:\\Program Files\\MySQL\\MySQL Server 8.0", "setup/mysql-server.msi");
            verifyAndInstall("C:\\Program Files\\MySQL\\MySQL Workbench 8.0", "setup/mysql-workbench.msi");

            System.out.println("[DB] Initializing database structures...");
            
            // Inicializar tabla de productos
            if (productTableService != null) {
                productTableService.createTable();
                System.out.println("[DB-SERVICE] Tabla productos lista.");
            }

            // Inicializar tabla de caja
            if (cajaService != null) {
                // Llamamos al método que crea la tabla en MySQL
                cajaService.inicializarInfraestructura();
                System.out.println("[DB-SERVICE] Tabla caja_movimientos lista.");
            } else {
                throw new Exception("CajaService could not be initialized.");
            }

            System.out.println("--- System Check Finished Successfully ---");
        } catch (Exception e) {
            System.err.println("--- System Check FAILED: " + e.getMessage() + " ---");
            // No cerramos el sistema con exit(1) para permitir que la app intente arrancar
        }
    }

    private void verifyAndInstall(String checkPath, String installerPath) throws Exception {
        if (!new File(checkPath).exists()) {
            System.out.println("[INSTALLER] Target not found: " + checkPath + ". Running setup...");
            ProcessBuilder pb = new ProcessBuilder("msiexec", "/i", installerPath, "/passive", "/norestart");
            int exitCode = pb.start().waitFor();
            if (exitCode != 0) throw new Exception("Error code: " + exitCode);
        } else {
            System.out.println("[OK] Already installed: " + checkPath);
        }
    }
}