package com.phamthehuy.doan.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class UnauthenticatedException extends RuntimeException {
    public UnauthenticatedException(String mess) {
        super(mess);
    }
}