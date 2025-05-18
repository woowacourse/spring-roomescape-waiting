package roomescape.global.auth;

import roomescape.member.domain.Member;

public record LoginMember(long id, String name, String email, String role) {

    public LoginMember(final Member member) {
        this(member.getId(), member.getName().getValue(), member.getEmail().getValue(),
                member.getRole().name());
    }
}
