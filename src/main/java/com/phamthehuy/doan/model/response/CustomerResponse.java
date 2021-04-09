package com.phamthehuy.doan.model.response;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class CustomerResponse {
    private Integer customerId;

    private String email;

    private String name;

    private Boolean gender;

    private String cardId;

    private String phone;

    private String address;

    private Date birthday;

    private String role = "CUSTOMER";

    private Integer accountBalance;

    private String image;

    private Boolean deleted;
}
