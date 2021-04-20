package com.phamthehuy.doan.model.request;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Getter
@Setter
public class PaymentRequest {
    @Min(value = 1, message = "Số tiền nạp ít nhất là 1 đô")
    @NotNull
    private Integer price;
}
