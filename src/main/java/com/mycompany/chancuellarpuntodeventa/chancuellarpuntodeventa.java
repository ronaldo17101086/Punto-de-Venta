package com.mycompany.chancuellarpuntodeventa;

import com.formdev.flatlaf.FlatLightLaf;
import com.mycompany.chancuellarpuntodeventa.login.LoginInterface;
import installers.EnvirometServiceToInit;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import javax.swing.UIManager;
import java.awt.EventQueue;

@SpringBootApplication
@ComponentScan(basePackages = {
    "com.mycompany.chancuellarpuntodeventa",
    "com.mycompany.chancuellarpuntodeventa.login",
    "Conexion",
    "installers",
    "ventas",
    "productoFrom",
    "producto"
})
public class chancuellarpuntodeventa {

    public static void main(String[] args) {
        // 1. Configurar el Look and Feel antes de que se creen los componentes
        setupLookAndFeel();

        // 2. Arrancar Spring UNA SOLA VEZ
        ApplicationContext context = new SpringApplicationBuilder(chancuellarpuntodeventa.class)
                .headless(false)
                .run(args);

        // 3. Ejecutar las validaciones de MySQL
        EnvirometServiceToInit setupService = context.getBean(EnvirometServiceToInit.class);
        setupService.runSetup();

        System.out.println("--- Proceso de inicialización terminado ---");

        // 4. Lanzar la interfaz gráfica
        EventQueue.invokeLater(() -> {
            try {
                LoginInterface login = context.getBean(LoginInterface.class);
                login.setVisible(true);
            } catch (Exception e) {
                System.err.println("Error al mostrar el Login: " + e.getMessage());
            }
        });
    }

    private static void setupLookAndFeel() {
        com.formdev.flatlaf.FlatLightLaf.setup();
        UIManager.put("Component.arc", 20);
        UIManager.put("TextComponent.arc", 20);
        UIManager.put("Button.arc", 20);
        UIManager.put("ScrollBar.thumbArc", 999);
        UIManager.put("ScrollBar.width", 8);
        UIManager.put("Component.focusWidth", 1);
    }
}
