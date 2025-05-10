package roomescape.member.dto.response;

import roomescape.member.model.Member;

public record MemberGetResponse(Long id, String name, String email) {

    public static MemberGetResponse from(Member member) {
        return new MemberGetResponse(member.getId(), member.getName(), member.getEmail());
    }
}
