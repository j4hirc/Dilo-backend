package com.example.dilo.DiloBackend.service.implementation;

import org.apache.xml.security.Init;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import xades4j.production.XadesBesSigningProfile;
import xades4j.production.XadesSigner;
import xades4j.providers.impl.KeyStoreKeyingDataProvider;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;


@Service
public class FirmaService {

    public PrivateKey obtenerLlavePrivada(String rutaP12, String password) throws Exception {
        KeyStore ks = KeyStore.getInstance("PKCS12");
        ks.load(new FileInputStream(rutaP12), password.toCharArray());
        String alias = ks.aliases().nextElement();
        return (PrivateKey) ks.getKey(alias, password.toCharArray());
    }

    public X509Certificate obtenerCertificado(String rutaP12, String password) throws Exception {
        KeyStore ks = KeyStore.getInstance("PKCS12");
        ks.load(new FileInputStream(rutaP12), password.toCharArray());
        String alias = ks.aliases().nextElement();
        return (X509Certificate) ks.getCertificate(alias);
    }

    static {
        Init.init();
    }

    public String firmarXML(String xmlSinFirma, byte[] p12Data, String password) throws Exception {
        // 1. Cargar el Keystore desde el array de bytes (sin necesidad de archivo físico)
        KeyStore ks = KeyStore.getInstance("PKCS12");
        ks.load(new ByteArrayInputStream(p12Data), password.toCharArray());
        String alias = ks.aliases().nextElement();

        // 2. Configurar el proveedor de llaves
        KeyStoreKeyingDataProvider keyingProvider = new KeyStoreKeyingDataProvider(
                alias,
                ks,
                new KeyStoreKeyingDataProvider.KeyStorePasswordRetriever() {
                    public char[] getPassword() { return password.toCharArray(); }
                });

        // 3. Configurar perfil XAdES-BES (el estándar del SRI)
        XadesBesSigningProfile profile = new XadesBesSigningProfile(keyingProvider);
        XadesSigner signer = profile.newSigner();

        // 4. Convertir String XML a Documento DOM
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true); // Muy importante para firmas XMLS
        Document doc = dbf.newDocumentBuilder().parse(new ByteArrayInputStream(xmlSinFirma.getBytes()));

        // 5. FIRMAR el documento
        signer.sign(doc.getDocumentElement());

        // 6. Retornar el XML firmado como String
        return XMLUtils.getFullTextChildrenFromElement(doc.getDocumentElement());
    }
}