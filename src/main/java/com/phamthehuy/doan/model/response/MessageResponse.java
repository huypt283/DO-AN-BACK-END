package com.phamthehuy.doan.model.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MessageResponse {
    String mess;
    public MessageResponse(String mess){
        this.mess=mess;
    }
}
