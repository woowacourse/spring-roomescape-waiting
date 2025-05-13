package roomescape.member.domain;

import lombok.Getter;
import lombok.NonNull;

@Getter
public class Member {

    private final Long id;

    @NonNull
    private final String name;

    @NonNull
    private final String email;

    @NonNull
    private final String password;

    @NonNull
    private final MemberRole role;

    public Member(final Long id, @NonNull final String name, @NonNull final String email,
                  @NonNull final String password,
                  @NonNull final MemberRole role) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public Member(final Long id, @NonNull final String name, @NonNull final String email,
                  @NonNull final String password,
                  @NonNull final String role) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        try {
            this.role = MemberRole.valueOf(role);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("유효하지 않은 회원 역할입니다.: " + role);
        }
    }

    public Member(@NonNull final String name, @NonNull final String email, @NonNull final String password) {
        this.id = null;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = MemberRole.MEMBER;
    }

    public Member(@NonNull Long id, @NonNull final String name, @NonNull final String email,
                  @NonNull final MemberRole role) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = null;
        this.role = role;
    }

    public boolean matchesPassword(final String password) {
        return this.password.equals(password);
    }

    public boolean isAdmin() {
        return this.role.isAdmin();
    }
}
