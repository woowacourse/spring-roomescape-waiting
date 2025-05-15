package roomescape.member.dto;

import roomescape.member.domain.Member;

public record MemberSignupRequest(String email, String password, String name) {
    public static Member from(MemberSignupRequest memberSignupRequest) {
        return new Member(memberSignupRequest.name, memberSignupRequest.email, memberSignupRequest.password);
    }
}
