package com.phamthehuy.doan.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicatedException extends RuntimeException {
    public DuplicatedException(String mess) {
        super(mess);
    }
}
