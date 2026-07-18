package com.example.dilo.DiloBackend.service.implementation;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Service
@Lazy
public class SriSoapClient {


    public boolean enviarRecepcion(byte[] xmlFirmadoBytes) {
        try {
            System.out.println("🌐 [MOCK] Simulando conexión HTTP con el WebService de Recepción del SRI...");
            Thread.sleep(100);

            System.out.println("✅ [MOCK] El SRI respondió: <estado>RECIBIDA</estado>");
            return true;

        } catch (Exception e) {
            return false;
        }
    }

    public boolean consultarAutorizacion(String claveAcceso) {
        try {
            System.out.println("🌐 [MOCK] Simulando conexión HTTP con el WebService de Autorización del SRI...");
            Thread.sleep(1500);

            System.out.println("✅ [MOCK] El SRI respondió: <estado>AUTORIZADO</estado> para la clave: " + claveAcceso);
            return true;

        } catch (Exception e) {
            return false;
        }
    }
}