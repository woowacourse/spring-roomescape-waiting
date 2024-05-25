package roomescape.service.dto.request;

import roomescape.domain.member.Member;
import roomescape.domain.member.Role;

public record CreateMemberRequest(String email, String password, String name) {

    public Member toMember(String encodedPassword, Role role) {
        return new Member(email, encodedPassword, name, role);
    }
}
