package com.example.dilo.DiloBackend.service.implementation;

import org.apache.xml.security.Init;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import xades4j.algorithms.EnvelopedSignatureTransform;
import xades4j.production.DataObjectReference;
import xades4j.production.SignedDataObjects;
import xades4j.production.XadesBesSigningProfile;
import xades4j.production.XadesSigner;
import xades4j.properties.DataObjectDesc;
import xades4j.providers.KeyingDataProvider;
import xades4j.providers.impl.DirectKeyingDataProvider;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;

@Service
public class FirmaService {

    static {
        Init.init();
    }

    public String firmarXML(String xmlSinFirma, byte[] p12Data, String password) throws Exception {

        KeyStore ks = KeyStore.getInstance("PKCS12");
        ks.load(new ByteArrayInputStream(p12Data), password.toCharArray());
        String alias = ks.aliases().nextElement();

        X509Certificate cert = (X509Certificate) ks.getCertificate(alias);
        PrivateKey key = (PrivateKey) ks.getKey(alias, password.toCharArray());

        KeyingDataProvider kdp = new DirectKeyingDataProvider(cert, key);

        XadesBesSigningProfile profile = new XadesBesSigningProfile(kdp);
        XadesSigner signer = profile.newSigner();

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true); // Muy importante para firmas XML
        Document doc = dbf.newDocumentBuilder().parse(new ByteArrayInputStream(xmlSinFirma.getBytes(StandardCharsets.UTF_8)));

        DataObjectDesc obj = new DataObjectReference("").withTransform(new EnvelopedSignatureTransform());
        SignedDataObjects dataObjs = new SignedDataObjects(obj);

        signer.sign(dataObjs, doc.getDocumentElement());

        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(doc), new StreamResult(writer));

        return writer.getBuffer().toString();
    }
}