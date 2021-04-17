package com.phamthehuy.doan.validation.impl;

import com.phamthehuy.doan.validation.ValidAddress;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class ValidAddressImpl implements ConstraintValidator<ValidAddress, String> {
    public void initialize(ValidAddress constraintAnnotation) {

    }

    public boolean isValid(String address, ConstraintValidatorContext context) {
        return address != null && address.split(",").length == 4;
    }
}
