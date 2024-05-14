package roomescape.member.dto;

import roomescape.member.model.Member;

public record MemberResponse(Long id, String name, String email) {
    public static MemberResponse from(final Member member) {
        return new MemberResponse(
                member.getId(),
                member.getName().value(),
                member.getEmail().value()
        );
    }
}
