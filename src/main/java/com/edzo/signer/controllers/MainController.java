package com.edzo.signer.controllers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.edzo.signer.services.SignerService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/signer")
@RequiredArgsConstructor
public class MainController {

  private final SignerService signerService;

  @Value("${SIGNER_INTERNAL_SECRET}")
  private String internalSecret;

  @Value("${SIGNER_KEYSTORE_PASSWORD}")
  private String keystorePassword;

  @PostMapping("/sign-xml")
  public ResponseEntity<String> sign(@RequestBody String invoice,
      @RequestHeader("X-Internal-Secret") String secret) {

    if (secret == null || !secret.equals(internalSecret)) {
      return ResponseEntity.status(403)
          .body("Acceso no autorizado");
    }

    if (invoice == null || invoice.isEmpty()) {
      return ResponseEntity.badRequest()
          .body("El campo 'xmlData' no puede estar vacío");
    }

    try {
      String signedXml = signerService.sign(invoice, "signature.p12", keystorePassword);

      return ResponseEntity.ok(signedXml);

    } catch (Exception e) {
      return ResponseEntity.status(500)
          .body("Error interno al firmar: " + e.getMessage());
    }
  }

}
