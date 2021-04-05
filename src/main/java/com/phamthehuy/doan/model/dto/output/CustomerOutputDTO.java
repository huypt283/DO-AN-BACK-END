package com.phamthehuy.doan.model.dto.output;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomerOutputDTO {
    private Integer customerId;

    private String name;

    private boolean gender;

    private String email;

    private String address;

    private String phone;

    private String cardId;

    private Long birthday;

    private int accountBalance;

    private String image;
}
