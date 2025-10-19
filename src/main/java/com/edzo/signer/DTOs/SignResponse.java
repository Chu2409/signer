package com.edzo.signer.DTOs;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SignResponse {
  private String signedXml;
  private boolean success;
  private String error;

  public static SignResponse error(String error) {
    return new SignResponse(null, false, error);
  }

  public static SignResponse success(String signedXml) {
    return new SignResponse(signedXml, true, null);
  }
}
