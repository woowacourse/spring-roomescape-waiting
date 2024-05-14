package roomescape.dto;

import roomescape.domain.Member;

public record LoginMember(Long id) {

    public static LoginMember from(Member member) {
        return new LoginMember(member.getId());
    }
}
