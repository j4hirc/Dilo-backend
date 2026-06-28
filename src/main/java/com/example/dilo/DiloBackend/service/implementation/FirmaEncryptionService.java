package com.example.dilo.DiloBackend.service.implementation;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.stereotype.Service;

@Service
public class FirmaEncryptionService {

    private final TextEncryptor encryptor;

    public FirmaEncryptionService(
            @Value("${firma.secret}") String secret,
            @Value("${firma.salt}") String salt) {
        this.encryptor = Encryptors.text(secret, salt);
    }

    public String encriptar(String passwordReal) {
        return encryptor.encrypt(passwordReal);
    }

    public String desencriptar(String passwordEncriptado) {
        return encryptor.decrypt(passwordEncriptado);
    }
}