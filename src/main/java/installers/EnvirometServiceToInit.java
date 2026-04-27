package installers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.io.File;

@Component
public class EnvirometServiceToInit {

    private final ProductTableService productTableService;

    // Usar constructor garantiza que el servicio no sea null
    @Autowired
    public EnvirometServiceToInit(ProductTableService productTableService) {
        this.productTableService = productTableService;
    }

    public void runSetup() {
        System.out.println("--- Starting System Check ---");
        try {
            verifyAndInstall("C:\\Program Files\\MySQL\\MySQL Server 8.0", "setup/mysql-server.msi");
            verifyAndInstall("C:\\Program Files\\MySQL\\MySQL Workbench 8.0", "setup/mysql-workbench.msi");

            if (productTableService != null) {
                System.out.println("[DB] Initializing database structures...");
                productTableService.createTable();
            } else {
                throw new Exception("ProductTableService could not be initialized.");
            }

            System.out.println("--- System Check Finished Successfully ---");
        } catch (Exception e) {
            System.err.println("--- System Check FAILED: " + e.getMessage() + " ---");
            System.exit(1); 
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