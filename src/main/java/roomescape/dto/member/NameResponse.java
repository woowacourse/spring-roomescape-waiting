package roomescape.dto.member;

import roomescape.domain.Member;

public record NameResponse(Long id, String name) {

    public static NameResponse from(Member member) {
        return new NameResponse(member.getId(), member.getName());
    }
}
