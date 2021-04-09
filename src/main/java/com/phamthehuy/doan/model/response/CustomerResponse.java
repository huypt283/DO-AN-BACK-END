package com.phamthehuy.doan.model.response;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

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

    private Date birthday;

    private String role = "CUSTOMER";

    private Integer accountBalance;

    private String image;

    private Boolean deleted;
}
