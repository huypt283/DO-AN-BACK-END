package com.phamthehuy.doan.model.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TransactionResponse {
    private Integer transactionId;
    private Integer amount;
    private Boolean payment;
    private String status;
    private String description;
    private String email;
}
