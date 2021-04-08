package com.phamthehuy.doan.model.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MessageResponse {
    String message;

    public MessageResponse(String mess) {
        this.message = mess;
    }
}
