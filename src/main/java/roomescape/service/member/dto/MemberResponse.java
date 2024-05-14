package roomescape.service.member.dto;

import roomescape.domain.member.Member;

public record MemberResponse(long id, String name, String email, String role) {
    public MemberResponse(Member member) {
        this(member.getId(), member.getMemberName(), member.getEmail(), member.getRole());
    }
}
