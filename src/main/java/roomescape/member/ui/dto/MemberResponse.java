package roomescape.member.ui.dto;

import roomescape.member.domain.Member;

public record MemberResponse(
        Long id,
        String name,
        String email

) {

    public record IdName(
            Long id,
            String name
    ) {
        public static IdName from(final Member member) {
            return new IdName(member.getId(), member.getName());
        }
    }

    public record Name(
            String name
    ) {
        public static Name from(final Member member) {
            return new Name(member.getName());
        }
    }
}
