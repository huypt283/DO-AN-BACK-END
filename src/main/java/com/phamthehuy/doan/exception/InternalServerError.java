package com.phamthehuy.doan.exception;

public class InternalServerError extends RuntimeException {
    public InternalServerError(String mess) {
        super(mess);
    }
}