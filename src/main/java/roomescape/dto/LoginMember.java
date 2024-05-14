package roomescape.dto;

import roomescape.domain.Member;

public record LoginMember(Long id) {
    public static LoginMember from(final Member member) {
        return new LoginMember(member.getId());
    }
}
