package com.edzo.signer.services;

import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.edzo.signer.utils.FilesUtil;

import lombok.RequiredArgsConstructor;

import xades4j.algorithms.EnvelopedSignatureTransform;
import xades4j.production.DataObjectReference;
import xades4j.production.SignedDataObjects;
import xades4j.production.XadesSigner;
import xades4j.properties.DataObjectDesc;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import xades4j.production.XadesBesSigningProfile;
import xades4j.providers.KeyingDataProvider;
import xades4j.providers.impl.DirectKeyingDataProvider;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

@Service
@RequiredArgsConstructor
public class SignerService {

  private final FilesUtil filesUtil;

  public String sign(String xmlString, String keyStorePath, String keyStorePassword)
      throws Exception {
    // Convertir el string XML a Document
    Document doc = parseXmlString(xmlString);

    DataObjectReference dataObjectReference = new DataObjectReference("#comprobante");
    DataObjectDesc signatureObject = dataObjectReference.withTransform(new EnvelopedSignatureTransform());
    SignedDataObjects signedDataObjects = new SignedDataObjects(signatureObject);

    Element signatureParent = doc.getDocumentElement();

    XadesSigner signer = getSigner(keyStorePath, keyStorePassword);
    signer.sign(signedDataObjects, signatureParent);

    return filesUtil.convertToString(doc);
  }

  private XadesSigner getSigner(String keyStorePath, String keyStorePassword) throws Exception {
    InputStream keyStoreStream = getInputStream(keyStorePath);
    KeyStore keyStore = loadKeyStore(keyStoreStream, keyStorePassword);
    String alias = getFirstAlias(keyStore);
    PrivateKey privateKey = getPrivateKey(keyStore, alias, keyStorePassword);
    X509Certificate cert = getCertificate(keyStore, alias);

    KeyingDataProvider kp = new DirectKeyingDataProvider(cert, privateKey);
    XadesBesSigningProfile profile = new XadesBesSigningProfile(kp);

    return profile.newSigner();
  }

  private InputStream getInputStream(String path) throws FileNotFoundException {
    InputStream inputStream = getClass().getClassLoader().getResourceAsStream(path);
    if (inputStream == null)
      throw new FileNotFoundException("The file was not found in the classpath: " + path);

    return inputStream;
  }

  private KeyStore loadKeyStore(InputStream keyStoreStream, String keyStorePassword)
      throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {
    KeyStore keyStore = KeyStore.getInstance("PKCS12");
    keyStore.load(keyStoreStream, keyStorePassword.toCharArray());

    return keyStore;
  }

  private String getFirstAlias(KeyStore keyStore) throws KeyStoreException {
    java.util.Enumeration<String> aliases = keyStore.aliases();
    if (!aliases.hasMoreElements()) {
      throw new KeyStoreException("No aliases found in the keystore.");
    }
    return aliases.nextElement();
  }

  private PrivateKey getPrivateKey(KeyStore keyStore, String alias, String password) throws Exception {
    PrivateKey privateKey = (PrivateKey) keyStore.getKey(alias, password.toCharArray());
    if (privateKey == null)
      throw new KeyStoreException("Failed to obtain private key from keystore.");

    return privateKey;
  }

  private X509Certificate getCertificate(KeyStore keyStore, String alias) throws Exception {
    X509Certificate certificate = (X509Certificate) keyStore.getCertificate(alias);
    if (certificate == null)
      throw new KeyStoreException("Failed to obtain certificate from keystore.");

    return certificate;
  }

  private Document parseXmlString(String xmlString) throws Exception {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setNamespaceAware(true);
    factory.setValidating(false);
    factory.setFeature("http://xml.org/sax/features/namespaces", false);
    factory.setFeature("http://xml.org/sax/features/validation", false);
    factory.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
    factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

    DocumentBuilder builder = factory.newDocumentBuilder();

    ByteArrayInputStream input = new ByteArrayInputStream(xmlString.getBytes("UTF-8"));
    Document doc = builder.parse(input);

    // Configurar el atributo id como ID válido
    Element rootElement = doc.getDocumentElement();
    if (rootElement.hasAttribute("id")) {
      rootElement.setIdAttribute("id", true);
    }

    return doc;
  }
}
