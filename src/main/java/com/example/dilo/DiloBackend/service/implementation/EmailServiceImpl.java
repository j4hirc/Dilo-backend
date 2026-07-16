package com.example.dilo.DiloBackend.service.implementation;

import com.example.dilo.DiloBackend.service.EmailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class EmailServiceImpl implements EmailService {

    @Value("${brevo.api.key}")
    private String brevoApiKey;

    @Value("${brevo.sender.email}")
    private String senderEmail;

    @Value("${brevo.sender.name}")
    private String senderName;

    private final String BREVO_API_URL = "https://api.brevo.com/v3/smtp/email";
    private final RestTemplate restTemplate = new RestTemplate();

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

        enviarPeticionBrevo(destinatarios, subject, htmlContent);
    }

    @Async
    @Override
    public void enviarFacturaSri(String emailCliente, String nombreCliente, String numeroFactura) {
        String subject = "Su Factura Electrónica #" + numeroFactura;

        // El texto incluye el recordatorio del SRI tal como lo planeaste
        String htmlContent = String.format(
                "<h3>Estimado/a %s,</h3>" +
                        "<p>Adjunto a este correo encontrará los detalles de su factura electrónica <b>#%s</b>.</p>" +
                        "<hr>" +
                        "<p><i>Le recordamos que puede verificar la validez de este comprobante directamente en el portal oficial del Servicio de Rentas Internas (SRI).</i></p>" +
                        "<p>Gracias por su preferencia.</p>",
                nombreCliente, numeroFactura
        );

        enviarPeticionBrevo(List.of(emailCliente), subject, htmlContent);
    }

    private void enviarPeticionBrevo(List<String> destinatarios, String subject, String htmlContent) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("api-key", brevoApiKey);

            List<Map<String, String>> toList = destinatarios.stream()
                    .map(email -> Map.of("email", email))
                    .collect(Collectors.toList());

            Map<String, Object> body = Map.of(
                    "sender", Map.of("name", senderName, "email", senderEmail),
                    "to", toList,
                    "subject", subject,
                    "htmlContent", htmlContent
            );

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            restTemplate.exchange(BREVO_API_URL, HttpMethod.POST, entity, String.class);

        } catch (Exception e) {
            System.err.println("Error al enviar correo vía Brevo: " + e.getMessage());
        }
    }
}