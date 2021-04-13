package com.phamthehuy.doan.validation;

import com.phamthehuy.doan.validation.impl.ValidNumberImpl;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidNumberImpl.class)
@Documented
public @interface ValidNumber {
    String message() default "Phải là số";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
