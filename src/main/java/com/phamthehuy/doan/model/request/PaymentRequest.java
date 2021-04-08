package com.phamthehuy.doan.model.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentRequest {
    private Double price;

    private String description;
}
