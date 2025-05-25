package roomescape.domain;

import lombok.Getter;

@Getter
public class LoginMember {

    private final Long id;

    private final String name;

    private final String email;

    private final Role role;

    private LoginMember(final Long id, final String name, final String email, final Role role) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
    }

    public static LoginMember from(final Member member) {
        return new LoginMember(member.getId(), member.getName(), member.getEmail(), member.getRole());
    }

    public static LoginMember from(final Long id, final String name, final String email, final Role role) {
        return new LoginMember(id, name, email, role);
    }

}
