package com.systechpro.utils;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Envío de correo vía SMTP (Gmail). Configuración por variables de entorno del
 * servidor (SMTP_HOST, SMTP_PORT, SMTP_USER, SMTP_PASS) - nunca en el código
 * ni en el repositorio. Si no están configuradas, el envío se omite en
 * silencio (permite seguir usando el sistema sin correo configurado).
 */
public class EmailService {
    private static final Logger LOGGER = Logger.getLogger(EmailService.class.getName());
    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(2, r -> {
        Thread t = new Thread(r, "email-sender");
        t.setDaemon(true);
        return t;
    });

    private static final String HOST = System.getenv("SMTP_HOST");
    private static final String PORT = System.getenv("SMTP_PORT");
    private static final String USER = System.getenv("SMTP_USER");
    private static final String PASS = System.getenv("SMTP_PASS");

    public static void enviarAsync(String destinatario, String asunto, String cuerpo) {
        if (HOST == null || PORT == null || USER == null || PASS == null || destinatario == null) {
            return;
        }
        EXECUTOR.submit(() -> enviar(destinatario, asunto, cuerpo));
    }

    /** Detiene el pool de envío. Se debe llamar al desplegar la aplicación (ver EmailShutdownListener). */
    public static void shutdown() {
        EXECUTOR.shutdown();
        try {
            // Espera a que un envío SMTP en curso termine antes de que Tomcat destruya el
            // classloader de la webapp; si no termina a tiempo, se interrumpe a la fuerza.
            if (!EXECUTOR.awaitTermination(5, TimeUnit.SECONDS)) {
                EXECUTOR.shutdownNow();
            }
        } catch (InterruptedException e) {
            EXECUTOR.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    private static void enviar(String destinatario, String asunto, String cuerpo) {
        try {
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", HOST);
            props.put("mail.smtp.port", PORT);

            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(USER, PASS);
                }
            });

            Message mensaje = new MimeMessage(session);
            mensaje.setFrom(new InternetAddress(USER, "SysTechPro"));
            mensaje.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinatario));
            mensaje.setSubject(asunto);
            mensaje.setText(cuerpo);

            Transport.send(mensaje);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error al enviar correo a " + destinatario, e);
        }
    }
}
