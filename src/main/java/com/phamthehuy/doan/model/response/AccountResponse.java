package com.phamthehuy.doan.model.response;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class AccountResponse {
    private Integer accountId;

    private String email;

    private String name;

    private Boolean gender;

    private String cardId;

    private String phone;

    private String address;

    private Date birthday;

    private String role;

    private Integer accountBalance = 0;

    private String image;

    private Boolean deleted;
}
