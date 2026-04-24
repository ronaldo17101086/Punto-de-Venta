package com.mycompany.chancuellarpuntodeventa;

import com.formdev.flatlaf.FlatLightLaf;
import com.mycompany.chancuellarpuntodeventa.login.LoginInterface;
import com.mycompany.chancuellarpuntodeventa.services.dashboard.dashboard;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import java.awt.EventQueue;
import javax.swing.UIManager;

@SpringBootApplication
@ComponentScan(basePackages = {
    "com.mycompany.chancuellarpuntodeventa",
    "com.mycompany.chancuellarpuntodeventa.login",
    "Conexion",
    "ventas",
    "productoFrom",
    "producto"
})
public class chancuellarpuntodeventa {

    public static void main(String[] args) {
        com.formdev.flatlaf.FlatLightLaf.setup(); // Activa el motor premium
        javax.swing.UIManager.put("Component.arc", 15); // Redondea TODO automáticamente
        // Esto activa el motor de renderizado suave para toda la app
        // Configuración global de suavizado y bordes premium
        UIManager.put("Component.arc", 20);
        UIManager.put("TextComponent.arc", 20);
        UIManager.put("Button.arc", 20);
        UIManager.put("ScrollBar.thumbArc", 999);
        UIManager.put("ScrollBar.width", 8);
        UIManager.put("Component.focusWidth", 1);
        FlatLightLaf.setup();

        ConfigurableApplicationContext context = new SpringApplicationBuilder(chancuellarpuntodeventa.class)
                .headless(false)
                .run(args);

        EventQueue.invokeLater(() -> {
            LoginInterface login = context.getBean(LoginInterface.class);
            login.setVisible(true);
        });
    }
}
//                                     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
