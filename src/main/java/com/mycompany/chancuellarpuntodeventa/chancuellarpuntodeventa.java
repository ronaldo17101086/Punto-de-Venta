package com.mycompany.chancuellarpuntodeventa;

import com.formdev.flatlaf.FlatLightLaf;
import com.mycompany.chancuellarpuntodeventa.services.dashboard.dashboard;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan; // Importante
import java.awt.EventQueue;

@SpringBootApplication
@ComponentScan(basePackages = {"com.mycompany.chancuellarpuntodeventa", "Conexion"})
@ComponentScan(basePackages = {"com.mycompany.chancuellarpuntodeventa", "ventas"})
@ComponentScan(basePackages = {"com.mycompany.chancuellarpuntodeventa", "producto"})
public class chancuellarpuntodeventa {

    public static void main(String[] args) {
        FlatLightLaf.setup();
        ConfigurableApplicationContext context = new SpringApplicationBuilder(chancuellarpuntodeventa.class)
                .headless(false)
                .run(args);
        EventQueue.invokeLater(() -> {
            dashboard dash = context.getBean(dashboard.class);
            dash.setVisible(true);
        });
    }
}
