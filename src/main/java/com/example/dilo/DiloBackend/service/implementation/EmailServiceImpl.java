package com.example.dilo.DiloBackend.service.implementation;

import com.example.dilo.DiloBackend.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${brevo.sender.email}")
    private String senderEmail;

    @Value("${brevo.sender.name}")
    private String senderName;

    @Async
    @Override
    public void enviarAlertaStockMinimo(List<String> destinatarios, String producto, String bodega, int cantidadActual, int stockMinimo) {
        if (destinatarios.isEmpty()) return;

        String subject = "⚠️ ALERTA: Stock Crítico - " + producto;
        String htmlContent = String.format(
                "<h3>Alerta de Inventario</h3>" +
                        "<p>El sistema ha detectado que un producto alcanzó su nivel crítico de stock.</p>" +
                        "<ul>" +
                        "<li><b>Producto:</b> %s</li>" +
                        "<li><b>Ubicación:</b> %s</li>" +
                        "<li><b>Cantidad Actual:</b> <span style='color:red;'>%d</span></li>" +
                        "<li><b>Stock Mínimo Configurado:</b> %d</li>" +
                        "</ul>" +
                        "<p>Por favor, gestione el reabastecimiento lo antes posible.</p>",
                producto, bodega, cantidadActual, stockMinimo
        );

        enviarPeticionSmtp(destinatarios, subject, htmlContent);
    }

    @Async
    @Override
    public void enviarFacturaSri(String emailCliente, String nombreCliente, String numeroFactura, byte[] pdfRide, byte[] xmlComprobante) {
        String subject = "Su Factura Electrónica #" + numeroFactura;

        String htmlContent = String.format(
                "<h3>Estimado/a %s,</h3>" +
                        "<p>Adjunto a este correo encontrará los detalles de su factura electrónica <b>#%s</b> en formato PDF y XML.</p>" +
                        "<hr>" +
                        "<p><i>Le recordamos que puede verificar la validez de este comprobante directamente en el portal oficial del Servicio de Rentas Internas (SRI).</i></p>" +
                        "<p>Gracias por su preferencia.</p>",
                nombreCliente, numeroFactura
        );

        enviarPeticionSmtpConAdjuntos(emailCliente, subject, htmlContent, pdfRide, xmlComprobante, numeroFactura);
    }

    private void enviarPeticionSmtpConAdjuntos(String destinatario, String subject, String htmlContent, byte[] pdf, byte[] xml, String numFactura) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(senderEmail, senderName);
            helper.setTo(destinatario);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            if (pdf != null && pdf.length > 0) {
                helper.addAttachment("Factura_" + numFactura + ".pdf", new org.springframework.core.io.ByteArrayResource(pdf));
            }
            if (xml != null && xml.length > 0) {
                helper.addAttachment("Factura_" + numFactura + ".xml", new org.springframework.core.io.ByteArrayResource(xml));
            }

            mailSender.send(message);
            System.out.println("Correo con XML y PDF enviado exitosamente a: " + destinatario);

        } catch (Exception e) {
            System.err.println("❌ Error enviando correo con adjuntos: " + e.getMessage());
        }
    }

    private void enviarPeticionSmtp(List<String> destinatarios, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(senderEmail, senderName);
            helper.setTo(destinatarios.toArray(new String[0]));
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            System.out.println("✅ Correo SMTP enviado exitosamente vía Brevo a: " + destinatarios);

        } catch (MessagingException | UnsupportedEncodingException e) {
            System.err.println("❌ Error construyendo el correo SMTP: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("❌ Error general al enviar correo SMTP: " + e.getMessage());
        }
    }

    @Async
    @Override
    public void enviarCodigoRecuperacion(String email, String codigo) {
        String subject = "🔑 Código de Recuperación - Dilo";
        String htmlContent = String.format(
                "<div style='font-family: Arial, sans-serif; text-align: center; color: #333;'>" +
                        "<h2>Recuperación de Contraseña</h2>" +
                        "<p>Has solicitado restablecer tu contraseña en el sistema <b>Dilo</b>.</p>" +
                        "<p>Tu código de verificación es:</p>" +
                        "<h1 style='color: #ed8936; letter-spacing: 5px; font-size: 32px;'>%s</h1>" +
                        "<p>Ingresa este código en la pantalla para crear una nueva contraseña.</p>" +
                        "<br><p style='font-size: 12px; color: #999;'>Si no solicitaste esto, puedes ignorar este correo de forma segura.</p>" +
                        "</div>", codigo
        );
        enviarPeticionSmtp(List.of(email), subject, htmlContent);
    }
}