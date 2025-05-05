package roomescape.dto.member;

import roomescape.domain.member.Member;
import roomescape.domain.member.Role;

public record LoginRequestDto(String email, String password) {

    public Member toEntity() {
        return new Member(null, email, password, Role.USER);
    }
}
