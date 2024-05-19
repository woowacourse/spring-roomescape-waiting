package roomescape.dto;

import roomescape.domain.member.Member;
import roomescape.domain.member.Role;

public record MemberResponse(long id, String name, String email, Role role) {
    public static MemberResponse from(Member member) {
        return new MemberResponse(member.getId(), member.getName(), member.getEmail().getEmail(), member.getRole());
    }
}
