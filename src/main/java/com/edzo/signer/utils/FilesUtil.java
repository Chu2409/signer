package com.edzo.signer.utils;

import java.io.ByteArrayOutputStream;
import java.io.StringWriter;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;

@Component
public class FilesUtil {
  public byte[] convertToByteArray(Document doc)
      throws TransformerFactoryConfigurationError, TransformerException, javax.xml.transform.TransformerException {
    Source source = new DOMSource(doc);

    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    Result result = new StreamResult(bos);

    Transformer transformer = TransformerFactory.newInstance().newTransformer();
    transformer.transform(source, result);

    return bos.toByteArray();
  }

  public String convertToString(Document doc)
      throws TransformerFactoryConfigurationError, TransformerException, javax.xml.transform.TransformerException {
    Source source = new DOMSource(doc);

    StringWriter writer = new StringWriter();
    Result result = new StreamResult(writer);

    Transformer transformer = TransformerFactory.newInstance().newTransformer();
    transformer.transform(source, result);

    return writer.toString();

  }
}
