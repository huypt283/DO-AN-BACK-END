package com.phamthehuy.doan.model.response;

import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

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

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date birthday;

    private String role = "CUSTOMER";

    private Integer accountBalance;

    private String image;

    private Boolean deleted;

    private Boolean enabled;
}
