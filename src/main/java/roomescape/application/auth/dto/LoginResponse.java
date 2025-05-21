package roomescape.application.auth.dto;

import roomescape.application.dto.MemberDto;

public record LoginResponse(String name) {

    public static LoginResponse from(MemberDto memberDto) {
        return new LoginResponse(memberDto.name());
    }
}
