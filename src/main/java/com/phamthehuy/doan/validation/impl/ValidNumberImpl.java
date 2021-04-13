package com.phamthehuy.doan.validation.impl;

import com.phamthehuy.doan.validation.ValidNumber;
import org.springframework.stereotype.Component;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

@Component
public class ValidNumberImpl implements ConstraintValidator<ValidNumber, String> {
    public void initialize(ValidNumber validNumber) {
    }

    public boolean isValid(String number, ConstraintValidatorContext constraintValidatorContext) {
        return number == null || number.matches("[0-9]+");
    }
}
