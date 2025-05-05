package roomescape.dto.member;

import roomescape.domain.member.Member;
import roomescape.domain.member.Role;

public class SignupRequestDto {

    private final String email;
    private final String password;

    public SignupRequestDto(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public Member toEntity() {
        return new Member(null, email, password, Role.USER);
    }
}
