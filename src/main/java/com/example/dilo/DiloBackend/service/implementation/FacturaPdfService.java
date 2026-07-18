package com.example.dilo.DiloBackend.service.implementation;

import com.example.dilo.DiloBackend.model.DetalleFactura;
import com.example.dilo.DiloBackend.model.Factura;
import com.example.dilo.DiloBackend.model.Negocio;
import org.openpdf.text.*;
import org.openpdf.text.pdf.PdfPCell;
import org.openpdf.text.pdf.PdfPTable;
import org.openpdf.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class FacturaPdfService {

    private static final Color AZUL_OSCURO = new Color(45, 62, 80);
    private static final Color GRIS_CLARO = new Color(247, 249, 252);
    private static final Color GRIS_BORDE = new Color(176, 176, 176);
    private static final Color GRIS_TEXTO = new Color(128, 128, 128);

    public byte[] generarPdfFactura(Factura factura, List<DetalleFactura> detalles) {
        Document document = new Document(PageSize.A4, 20, 20, 20, 20);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            Negocio negocio = factura.getNegocio();

            // ---- Encabezado: negocio (izq) + caja de factura (der) ----
            PdfPTable header = new PdfPTable(2);
            header.setWidthPercentage(100);
            header.setWidths(new float[]{1.2f, 1f});

            PdfPCell celdaNegocio = new PdfPCell();
            celdaNegocio.setBorder(Rectangle.NO_BORDER);
            try (InputStream logoStream = getClass().getResourceAsStream("/reportes/logo_dilo.png")) {
                if (logoStream != null) {
                    Image logo = Image.getInstance(logoStream.readAllBytes());
                    logo.scaleToFit(100, 40);
                    celdaNegocio.addElement(logo);
                }
            } catch (Exception ignored) { }
            celdaNegocio.addElement(new Paragraph(negocio.getRazonSocial(),
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16)));
            celdaNegocio.addElement(new Paragraph("RUC: " + negocio.getRuc(),
                    FontFactory.getFont(FontFactory.HELVETICA, 12)));
            celdaNegocio.addElement(new Paragraph("Dirección Matriz: " + negocio.getDireccion(),
                    FontFactory.getFont(FontFactory.HELVETICA, 10)));
            header.addCell(celdaNegocio);

            PdfPCell celdaFactura = new PdfPCell();
            celdaFactura.setBackgroundColor(GRIS_CLARO);
            celdaFactura.setBorderColor(GRIS_BORDE);
            celdaFactura.setPadding(10);
            celdaFactura.addElement(new Paragraph("FACTURA   No. " + factura.getNumeroFactura(),
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14)));
            celdaFactura.addElement(new Paragraph("Fecha Emisión: " +
                    factura.getFechaEmision().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                    FontFactory.getFont(FontFactory.HELVETICA, 10)));
            celdaFactura.addElement(new Paragraph("Clave de Acceso:",
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10)));
            Paragraph clave = new Paragraph(factura.getClaveAccesoSri(),
                    FontFactory.getFont(FontFactory.HELVETICA, 9));
            clave.setAlignment(Element.ALIGN_CENTER);
            celdaFactura.addElement(clave);
            header.addCell(celdaFactura);

            document.add(header);
            document.add(new Paragraph(" "));

            // ---- Datos del cliente ----
            PdfPTable cliente = new PdfPTable(4);
            cliente.setWidthPercentage(100);
            cliente.setWidths(new float[]{2f, 4f, 1.5f, 2.5f});
            agregarCelda(cliente, "Razón Social:", true, Element.ALIGN_LEFT);
            agregarCelda(cliente, factura.getCliente().getPrimerNombre() + " " +
                    factura.getCliente().getApellidoPaterno(), false, Element.ALIGN_LEFT);
            agregarCelda(cliente, "RUC/DNI:", true, Element.ALIGN_LEFT);
            agregarCelda(cliente, factura.getCliente().getDni(), false, Element.ALIGN_LEFT);
            document.add(cliente);
            document.add(new Paragraph(" "));

            // ---- Tabla de productos ----
            PdfPTable tabla = new PdfPTable(4);
            tabla.setWidthPercentage(100);
            tabla.setWidths(new float[]{1f, 4f, 1.5f, 1.5f});

            Font fontHeaderBlanco = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE);
            for (String titulo : new String[]{"Cantidad", "Descripción del Producto", "Precio Unit.", "Total"}) {
                PdfPCell celda = new PdfPCell(new Phrase(titulo, fontHeaderBlanco));
                celda.setBackgroundColor(AZUL_OSCURO);
                celda.setPadding(6);
                tabla.addCell(celda);
            }

            Font fontNormal = FontFactory.getFont(FontFactory.HELVETICA, 10);
            for (DetalleFactura df : detalles) {
                tabla.addCell(celdaSimple(String.valueOf(df.getCantidad()), fontNormal, Element.ALIGN_LEFT));
                tabla.addCell(celdaSimple(df.getProducto().getNombre(), fontNormal, Element.ALIGN_LEFT));
                tabla.addCell(celdaSimple(formatMoney(df.getPrecioUnitario()), fontNormal, Element.ALIGN_RIGHT));
                tabla.addCell(celdaSimple(formatMoney(df.getSubtotalItem()), fontNormal, Element.ALIGN_RIGHT));
            }
            document.add(tabla);
            document.add(new Paragraph(" "));

            // ---- Totales ----
            BigDecimal subtotal = factura.getSubtotalIva0().add(factura.getSubtotalIvaAplicado());

            PdfPTable totales = new PdfPTable(2);
            totales.setWidthPercentage(50);
            totales.setHorizontalAlignment(Element.ALIGN_RIGHT);

            Font fontEtiqueta = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
            Font fontValor = FontFactory.getFont(FontFactory.HELVETICA, 10);
            Font fontTotalLabel = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);

            agregarFilaTotal(totales, "SUBTOTAL:", formatMoney(subtotal), fontEtiqueta, fontValor);
            agregarFilaTotal(totales, "IVA:", formatMoney(factura.getTotalIva()), fontEtiqueta, fontValor);
            agregarFilaTotal(totales, "TOTAL:", formatMoney(factura.getTotalFactura()), fontTotalLabel, fontTotalLabel);

            document.add(totales);

            Paragraph footer = new Paragraph("Documento generado por Dilo - Facturación Inteligente",
                    FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 9, GRIS_TEXTO));
            footer.setSpacingBefore(15);
            document.add(footer);

            document.close();
            return out.toByteArray();

        } catch (Exception e) {
            System.err.println("❌ Fallo en la generación del PDF de la factura: " + e.getMessage());
            return new byte[0];
        }
    }

    private void agregarCelda(PdfPTable table, String texto, boolean negrita, int alineacion) {
        Font font = negrita ? FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10)
                : FontFactory.getFont(FontFactory.HELVETICA, 10);
        PdfPCell celda = new PdfPCell(new Phrase(texto, font));
        celda.setHorizontalAlignment(alineacion);
        celda.setBorderColor(GRIS_BORDE);
        celda.setPadding(5);
        table.addCell(celda);
    }

    private PdfPCell celdaSimple(String texto, Font font, int alineacion) {
        PdfPCell celda = new PdfPCell(new Phrase(texto, font));
        celda.setHorizontalAlignment(alineacion);
        celda.setPadding(5);
        return celda;
    }

    private void agregarFilaTotal(PdfPTable table, String etiqueta, String valor, Font fontEtiqueta, Font fontValor) {
        PdfPCell celdaEtiqueta = new PdfPCell(new Phrase(etiqueta, fontEtiqueta));
        celdaEtiqueta.setBorder(Rectangle.NO_BORDER);
        table.addCell(celdaEtiqueta);

        PdfPCell celdaValor = new PdfPCell(new Phrase(valor, fontValor));
        celdaValor.setBorder(Rectangle.NO_BORDER);
        celdaValor.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(celdaValor);
    }

    private String formatMoney(BigDecimal value) {
        return "$ " + value.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }
}