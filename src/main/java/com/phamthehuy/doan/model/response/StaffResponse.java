package com.phamthehuy.doan.model.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StaffResponse {
    private Integer staffId;

    private String email;

    private String name;

    private String cardId;

    private Long birthday;

    private Boolean gender;

    private Boolean role;

    private String address;

    private String phone;

    private String image;
}
