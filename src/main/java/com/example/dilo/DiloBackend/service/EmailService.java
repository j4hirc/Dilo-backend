package com.example.dilo.DiloBackend.service;

import java.util.List;

public interface EmailService {
    void enviarAlertaStockMinimo(List<String> destinatarios, String producto, String bodega, int cantidadActual, int stockMinimo);

    void enviarFacturaSri(String emailCliente, String nombreCliente, String numeroFactura, byte[] pdfRide, byte[] xmlComprobante);
}