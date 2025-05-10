package roomescape.dto.response;

import roomescape.domain.Member;

public record MemberGetResponse(Long id, String name, String email) {

    public static MemberGetResponse from(Member member) {
        return new MemberGetResponse(member.getId(), member.getName(), member.getEmail());
    }
}
