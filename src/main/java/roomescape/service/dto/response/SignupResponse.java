package roomescape.service.dto.response;

import roomescape.domain.Member;
import roomescape.domain.MemberRole;

public record SignupResponse(Long id, String name, String email, MemberRole role) {
    public SignupResponse(Member member) {
        this(member.getId(), member.getName(), member.getEmail(), member.getRole());
    }
}
