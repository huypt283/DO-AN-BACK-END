package com.phamthehuy.doan.model.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomerResponse {
    private Integer customerId;

    private String name;

    private Boolean gender;

    private String email;

    private String address;

    private String phone;

    private String cardId;

    private Long birthday;

    private Integer accountBalance;

    private String image;
}
