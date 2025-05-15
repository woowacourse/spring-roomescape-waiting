package roomescape.global.auth;

import roomescape.member.domain.Member;

public record LoginMember(long id, String name, String email, String role) {

    public LoginMember(final Member member) {
        this(member.getId(), member.getName(), member.getEmail(), member.getRole().name());
    }
}
