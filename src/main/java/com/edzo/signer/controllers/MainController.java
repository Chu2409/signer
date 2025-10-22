package com.edzo.signer.controllers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.edzo.signer.services.SignerService;
import com.edzo.signer.DTOs.SignRequest;
import com.edzo.signer.DTOs.SignResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/signer")
@RequiredArgsConstructor
@Slf4j
public class MainController {

  private final SignerService signerService;

  @Value("${SIGNER_INTERNAL_SECRET}")
  private String internalSecret;

  @Value("${SIGNER_KEYSTORE_PASSWORD}")
  private String keystorePassword;

  @Value("${SIGNATURE_P12_PATH}")
  private String keystorePath;

  @PostMapping("/sign-xml")
  public ResponseEntity<SignResponse> sign(@RequestBody SignRequest request,
      @RequestHeader("X-Internal-Secret") String secret) {

    log.info("Starting to sign XML");

    if (secret == null || !secret.equals(internalSecret)) {
      log.error("Access denied");
      return ResponseEntity.status(403)
          .body(SignResponse.error("Acceso no autorizado"));
    }

    if (request == null || request.getXmlData() == null || request.getXmlData().trim().isEmpty()) {
      log.error("XML data is empty");
      return ResponseEntity.badRequest()
          .body(SignResponse.error("El campo 'xmlData' no puede estar vacío"));
    }

    try {
      String signedXml = signerService.sign(request.getXmlData(), keystorePath, keystorePassword);
      log.info("XML signed successfully");
      return ResponseEntity.ok(SignResponse.success(signedXml));

    } catch (Exception e) {
      log.error("Error signing XML: {}", e.getMessage());
      return ResponseEntity.status(500)
          .body(SignResponse.error("Error interno al firmar: " + e.getMessage()));
    }
  }
  // @PostMapping("/test")
  // public ResponseEntity<String> test(@RequestBody String invoice,
  // @RequestHeader("X-Internal-Secret") String secret) {
  // log.info("Starting to test XML");

  // if (secret == null || !secret.equals(internalSecret)) {
  // log.error("Access denied");
  // return ResponseEntity.status(403)
  // .body("Acceso no autorizado");
  // }

  // if (invoice == null || invoice.isEmpty()) {
  // log.error("XML data is empty");
  // return ResponseEntity.badRequest()
  // .body("El campo 'xmlData' no puede estar vacío");
  // }

  // try {
  // String signedXml = signerService.sign(invoice, "signature.p12",
  // keystorePassword);
  // log.info("XML signed successfully");
  // return ResponseEntity.ok(signedXml);

  // } catch (Exception e) {
  // log.error("Error signing XML: {}", e.getMessage());
  // return ResponseEntity.status(500)
  // .body("Error interno al firmar: " + e.getMessage());
  // }
  // }

}
