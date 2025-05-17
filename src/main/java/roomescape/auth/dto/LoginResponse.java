package roomescape.auth.dto;

import roomescape.exception.custom.reason.ResponseInvalidException;

public record LoginResponse(String name) {

    public LoginResponse{
        if(name == null){
            throw new ResponseInvalidException();
        }
    }
}
