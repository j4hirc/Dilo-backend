package com.example.dilo.DiloBackend.service.implementation;

import com.example.dilo.DiloBackend.model.DetalleFactura;
import com.example.dilo.DiloBackend.model.Factura;
import com.example.dilo.DiloBackend.model.Negocio;
import com.example.dilo.DiloBackend.repository.DetalleFacturaRepository;
import com.example.dilo.DiloBackend.repository.FacturaRepository;
import com.example.dilo.DiloBackend.service.EmailService;
import com.example.dilo.DiloBackend.service.SriService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SriServiceImpl implements SriService {

    private final FacturaRepository facturaRepository;
    private final DetalleFacturaRepository detalleFacturaRepository;
    private final EmailService emailService;

    @Async
    @Override
    public void procesarFacturaElectronica(Factura factura) {
        try {
            System.out.println("⏳ Iniciando proceso SRI para factura ID: " + factura.getId());

            String claveAcceso = generarClaveAcceso(factura);
            factura.setClaveAccesoSri(claveAcceso);
            factura.setEstadoSri("PROCESANDO");
            facturaRepository.save(factura);

            List<DetalleFactura> detalles = detalleFacturaRepository.findByFacturaId(factura.getId());

            String xmlFactura = generarXMLFactura(factura, detalles, claveAcceso);
            byte[] xmlBytes = xmlFactura.getBytes(StandardCharsets.UTF_8);


            factura.setEstadoSri("AUTORIZADO");
            facturaRepository.save(factura);

            // 2. Compilar y Generar el PDF (RIDE)
            byte[] pdfBytes = generarPdfRide(factura, detalles);

            // 3. Despachar el correo al cliente
            if (pdfBytes.length > 0) {
                emailService.enviarFacturaSri(
                        factura.getCliente().getEmail(),
                        factura.getCliente().getPrimerNombre(),
                        factura.getNumeroFactura(),
                        pdfBytes,
                        xmlBytes
                );
            } else {
                System.err.println("⚠️ El PDF se generó vacío, revisa la plantilla Jasper.");
            }

            System.out.println("✅ Flujo documental completado exitosamente.");

        } catch (Exception e) {
            System.err.println("❌ Error procesando factura en el SRI: " + e.getMessage());
            factura.setEstadoSri("ERROR_SRI");
            facturaRepository.save(factura);
        }
    }


    private byte[] generarPdfRide(Factura factura, List<DetalleFactura> detalles) {
        try {
            JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(detalles);


            Map<String, Object> parametros = new HashMap<>();
            InputStream logoStream = getClass().getResourceAsStream("/reportes/logo_dilo.png");
            parametros.put("LOGO_DILO", logoStream);
            parametros.put("NUMERO_FACTURA", factura.getNumeroFactura());
            parametros.put("FECHA_EMISION", factura.getFechaEmision().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            parametros.put("CLAVE_ACCESO", factura.getClaveAccesoSri());
            parametros.put("RAZON_SOCIAL", factura.getNegocio().getRazonSocial());
            parametros.put("RUC", factura.getNegocio().getRuc());
            parametros.put("DIRECCION", factura.getNegocio().getDireccion());
            parametros.put("CLIENTE_NOMBRE", factura.getCliente().getPrimerNombre() + " " + factura.getCliente().getApellidoPaterno());
            parametros.put("CLIENTE_DNI", factura.getCliente().getDni());
            parametros.put("SUBTOTAL", factura.getSubtotalIva0().add(factura.getSubtotalIvaAplicado()));
            parametros.put("IVA", factura.getTotalIva());
            parametros.put("TOTAL", factura.getTotalFactura());

            // Ubicación de la plantilla base en tu proyecto
            InputStream reportStream = getClass().getResourceAsStream("/reportes/factura.jrxml");

            if (reportStream == null) {
                throw new RuntimeException("No se encontró el archivo factura.jrxml en src/main/resources/reportes/");
            }

            JasperReport jasperReport = JasperCompileManager.compileReport(reportStream);
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parametros, dataSource);

            return JasperExportManager.exportReportToPdf(jasperPrint);

        } catch (Exception e) {
            System.err.println("❌ Fallo en la generación del PDF de JasperReports: " + e.getMessage());
            return new byte[0];
        }
    }

    private String generarClaveAcceso(Factura factura) {
        Negocio negocio = factura.getNegocio();
        String fecha = factura.getFechaEmision().format(DateTimeFormatter.ofPattern("ddMMyyyy"));
        String tipoComprobante = "01";
        String ruc = negocio.getRuc();
        String ambiente = "1";
        String serie = "001001";
        String secuencial = String.format("%09d", factura.getId());
        String codigoNumerico = "12345678";
        String tipoEmision = "1";

        String claveSinDigito = fecha + tipoComprobante + ruc + ambiente + serie + secuencial + codigoNumerico + tipoEmision;
        int digitoVerificador = calcularModulo11(claveSinDigito);

        return claveSinDigito + digitoVerificador;
    }

    private int calcularModulo11(String cadena) {
        int baseMultiplicador = 7;
        int[] multiplicadores = {2, 3, 4, 5, 6, 7};
        int multiplicadorIndex = 0;
        int total = 0;

        for (int i = cadena.length() - 1; i >= 0; i--) {
            total += Character.getNumericValue(cadena.charAt(i)) * multiplicadores[multiplicadorIndex];
            multiplicadorIndex = (multiplicadorIndex + 1) % multiplicadores.length;
        }

        int residuo = total % 11;
        int digito = 11 - residuo;

        if (digito == 11) return 0;
        if (digito == 10) return 1;
        return digito;
    }


    private String generarXMLFactura(Factura factura, List<DetalleFactura> detalles, String claveAcceso) {
        StringBuilder xml = new StringBuilder();
        Negocio negocio = factura.getNegocio();

        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<factura id=\"comprobante\" version=\"1.1.0\">\n");

        xml.append("  <infoTributaria>\n");
        xml.append("    <ambiente>1</ambiente>\n"); // 1 Pruebas, 2 Producción
        xml.append("    <tipoEmision>1</tipoEmision>\n"); // Emisión Normal
        xml.append("    <razonSocial>").append(negocio.getRazonSocial()).append("</razonSocial>\n");
        xml.append("    <ruc>").append(negocio.getRuc()).append("</ruc>\n");
        xml.append("    <claveAcceso>").append(claveAcceso).append("</claveAcceso>\n");
        xml.append("    <codDoc>01</codDoc>\n"); // 01 Factura
        xml.append("    <estab>001</estab>\n");
        xml.append("    <ptoEmi>001</ptoEmi>\n");
        xml.append("    <secuencial>").append(String.format("%09d", factura.getId())).append("</secuencial>\n");
        xml.append("    <dirMatriz>").append(negocio.getDireccion()).append("</dirMatriz>\n");
        xml.append("  </infoTributaria>\n");

        xml.append("  <infoFactura>\n");
        xml.append("    <fechaEmision>").append(factura.getFechaEmision().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))).append("</fechaEmision>\n");
        xml.append("    <dirEstablecimiento>").append(negocio.getDireccion()).append("</dirEstablecimiento>\n");
        xml.append("    <obligadoContabilidad>NO</obligadoContabilidad>\n");

        String tipoIdComprador = factura.getCliente().getDni().length() == 13 ? "04" : "05";
        if(factura.getCliente().getDni().equals("9999999999999")) tipoIdComprador = "07";

        xml.append("    <tipoIdentificacionComprador>").append(tipoIdComprador).append("</tipoIdentificacionComprador>\n");
        xml.append("    <razonSocialComprador>").append(factura.getCliente().getPrimerNombre()).append(" ").append(factura.getCliente().getApellidoPaterno()).append("</razonSocialComprador>\n");
        xml.append("    <identificacionComprador>").append(factura.getCliente().getDni()).append("</identificacionComprador>\n");

        BigDecimal totalSinImpuestos = factura.getSubtotalIva0().add(factura.getSubtotalIvaAplicado());
        xml.append("    <totalSinImpuestos>").append(totalSinImpuestos).append("</totalSinImpuestos>\n");
        xml.append("    <totalDescuento>").append(factura.getTotalDescuento()).append("</totalDescuento>\n");

        xml.append("    <totalConImpuestos>\n");
        if (factura.getSubtotalIvaAplicado().compareTo(BigDecimal.ZERO) > 0) {
            xml.append("      <totalImpuesto>\n");
            xml.append("        <codigo>2</codigo>\n"); // 2 es IVA
            xml.append("        <codigoPorcentaje>4</codigoPorcentaje>\n"); // 4 es 15% (Según tabla actual SRI)
            xml.append("        <baseImponible>").append(factura.getSubtotalIvaAplicado()).append("</baseImponible>\n");
            xml.append("        <valor>").append(factura.getTotalIva()).append("</valor>\n");
            xml.append("      </totalImpuesto>\n");
        }
        xml.append("    </totalConImpuestos>\n");
        xml.append("    <propina>0.00</propina>\n");
        xml.append("    <importeTotal>").append(factura.getTotalFactura()).append("</importeTotal>\n");
        xml.append("    <moneda>DOLAR</moneda>\n");

        xml.append("    <pagos>\n");
        xml.append("      <pago>\n");
        xml.append("        <formaPago>01</formaPago>\n");
        xml.append("        <total>").append(factura.getTotalFactura()).append("</total>\n");
        xml.append("      </pago>\n");
        xml.append("    </pagos>\n");
        xml.append("  </infoFactura>\n");


        xml.append("  <detalles>\n");
        for (DetalleFactura df : detalles) {
            xml.append("    <detalle>\n");
            xml.append("      <codigoPrincipal>").append(df.getProducto().getCodigoPrincipal()).append("</codigoPrincipal>\n");
            xml.append("      <descripcion>").append(df.getProducto().getNombre()).append("</descripcion>\n");
            xml.append("      <cantidad>").append(df.getCantidad()).append("</cantidad>\n");
            xml.append("      <precioUnitario>").append(df.getPrecioUnitario()).append("</precioUnitario>\n");
            xml.append("      <descuento>").append(df.getDescuento()).append("</descuento>\n");
            xml.append("      <precioTotalSinImpuesto>").append(df.getSubtotalItem()).append("</precioTotalSinImpuesto>\n");

            xml.append("      <impuestos>\n");
            xml.append("        <impuesto>\n");
            xml.append("          <codigo>2</codigo>\n");

            if (df.getProducto().getGrabaIva()) {
                xml.append("          <codigoPorcentaje>4</codigoPorcentaje>\n");
                xml.append("          <tarifa>15.00</tarifa>\n");
                BigDecimal valorIva = df.getSubtotalItem().multiply(new BigDecimal("0.15"));
                xml.append("          <baseImponible>").append(df.getSubtotalItem()).append("</baseImponible>\n");
                xml.append("          <valor>").append(valorIva.setScale(2, java.math.RoundingMode.HALF_UP)).append("</valor>\n");
            } else {
                xml.append("          <codigoPorcentaje>0</codigoPorcentaje>\n");
                xml.append("          <tarifa>0.00</tarifa>\n");
                xml.append("          <baseImponible>").append(df.getSubtotalItem()).append("</baseImponible>\n");
                xml.append("          <valor>0.00</valor>\n");
            }

            xml.append("        </impuesto>\n");
            xml.append("      </impuestos>\n");
            xml.append("    </detalle>\n");
        }
        xml.append("  </detalles>\n");
        xml.append("</factura>");

        return xml.toString();
    }
}