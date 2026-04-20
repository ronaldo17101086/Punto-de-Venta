package com.mycompany.chancuellarpuntodeventa;

import com.formdev.flatlaf.FlatLightLaf;
import com.mycompany.chancuellarpuntodeventa.login.LoginInterface;
import com.mycompany.chancuellarpuntodeventa.services.dashboard.dashboard;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import java.awt.EventQueue;

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
