package com.phamthehuy.doan.validation;


import com.phamthehuy.doan.validation.impl.ValidAddressImpl;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidAddressImpl.class)
@Documented
public @interface ValidAddress {
    String message() default "Địa chỉ không hợp lệ";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
