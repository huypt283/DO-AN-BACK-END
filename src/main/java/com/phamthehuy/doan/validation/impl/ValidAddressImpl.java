package com.phamthehuy.doan.validation.impl;

import com.phamthehuy.doan.validation.ValidBirthday;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class ValidAddressImpl implements ConstraintValidator<ValidBirthday, String> {
    public void initialize(ValidBirthday constraintAnnotation) {

    }

    public boolean isValid(String address, ConstraintValidatorContext context) {
        return address != null && address.split(",").length == 4;
    }
}
