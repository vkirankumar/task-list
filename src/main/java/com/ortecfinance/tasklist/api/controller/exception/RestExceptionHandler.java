package com.ortecfinance.tasklist.api.controller.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.io.IOException;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        HttpServletRequest webRequest = ((ServletWebRequest) request).getRequest();
        if ("/projects".equals(webRequest.getServletPath()) && HttpMethod.POST.name().equals(webRequest.getMethod())) {
            return new ResponseEntity<>("Invalid project name!!", status);
        }
        return new ResponseEntity<>("!!", status);
    }

    @ExceptionHandler(DateTimeParseException.class)
    protected ResponseEntity<Object> handleDateTimeParseException(DateTimeParseException ex, WebRequest request) throws IOException {
        Map<String, String> error = new HashMap<>();
        error.put("message", String.format("Invalid Date Format!! => %s", ex.getMessage()));
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }
}
