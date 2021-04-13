package com.phamthehuy.doan.validation.impl;

import com.phamthehuy.doan.validation.ValidBirthday;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Date;

public class ValidBirthdayImpl implements ConstraintValidator<ValidBirthday, Date> {
    public void initialize(ValidBirthday constraintAnnotation) {

    }

    public boolean isValid(Date birthday, ConstraintValidatorContext context) {
        return birthday != null && birthday.before(new Date());
    }
}
