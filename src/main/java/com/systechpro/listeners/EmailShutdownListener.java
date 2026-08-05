package com.systechpro.listeners;

import com.systechpro.utils.EmailService;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

/**
 * Detiene el pool de hilos de EmailService al desplegar la aplicación.
 * Sin esto, Tomcat reporta un memory leak (hilos "email-sender" que
 * sobreviven al redeploy referenciando el classloader de la webapp vieja).
 */
@WebListener
public class EmailShutdownListener implements ServletContextListener {
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        EmailService.shutdown();
    }
}
