package com.phamthehuy.doan.exception;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
@RestController
public class CustomHandleException extends ResponseEntityExceptionHandler {
    @ExceptionHandler(CustomException.class)    //bắt loại exception
    public ResponseEntity<Object> handleCustomException(Exception ex, WebRequest request){
        Map<String, String> map=new HashMap<>();
        map.put("mess", ex.getMessage());
        return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST); //trả về json lỗi và loại lỗi
    }

    @ExceptionHandler(UnauthenticatedException.class)
    public ResponseEntity<Object> handleUnauthenticatedException(Exception ex, WebRequest request){
        Map<String, String> map=new HashMap<>();
        map.put("mess", "Token không hợp lệ");
        return new ResponseEntity<>(map, HttpStatus.UNAUTHORIZED);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return new ResponseEntity<Object>(errors, HttpStatus.BAD_REQUEST);
    }

}
