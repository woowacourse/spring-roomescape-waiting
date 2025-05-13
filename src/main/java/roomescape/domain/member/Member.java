package roomescape.domain.member;

import org.springframework.security.crypto.password.PasswordEncoder;

public class Member {
    private final Long id;
    private final MemberName name;
    private final MemberEmail email;
    private final MemberEncodedPassword password;
    private final MemberRole role;

    public Member(
            final Long id,
            final MemberName name,
            final MemberEmail email,
            final MemberEncodedPassword password,
            final MemberRole role
    ) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public boolean isMatchPassword(final MemberPassword rawPassword, final PasswordEncoder encoder) {
        return password.isMatched(rawPassword, encoder);
    }

    public Long getId() {
        return id;
    }

    public MemberName getName() {
        return name;
    }

    public MemberEmail getEmail() {
        return email;
    }

    public MemberEncodedPassword getPassword() {
        return password;
    }

    public MemberRole getRole() {
        return role;
    }
}
