package com.phamthehuy.doan.model.dto.input;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StaffUpdateDTO {
    private Integer staffId;

    private String email;

    private String name;

    private String cardId;

    private long birthday;

    private boolean gender;

    private boolean role;

    private String address;

    private String phone;

    private String image;
}
