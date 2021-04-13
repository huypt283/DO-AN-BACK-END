package com.phamthehuy.doan.validation;


import com.phamthehuy.doan.validation.impl.ValidBirthdayImpl;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidBirthdayImpl.class)
@Documented
public @interface ValidBirthday {
    String message() default "Phải là số";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
