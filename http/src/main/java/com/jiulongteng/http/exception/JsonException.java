package com.jiulongteng.http.exception;

import java.io.IOException;

public class JsonException extends IOException {

  public JsonException() {
    super();
  }
  public JsonException(String message) {
    super(message);
  }

  public JsonException(String message, Throwable cause) {
    super(message, cause);
  }

  public JsonException(Throwable cause) {
    super(cause);
  }
}