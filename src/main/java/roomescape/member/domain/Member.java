package roomescape.member.domain;

import java.util.Objects;

public class Member {
    private final Long id;
    private final MemberName name;
    private final String email;
    private final MemberRole role;

    public Member(Long id, String name, String email) {
        this(id, new MemberName(name), email, MemberRole.USER);
    }

    public Member(Long id, String name, String email, String role) {
        this(id, new MemberName(name), email, MemberRole.valueOf(role));
    }

    private Member(Long id, MemberName name, String email, MemberRole role) {
        this.id = Objects.requireNonNull(id);
        this.name = Objects.requireNonNull(name);
        this.email = Objects.requireNonNull(email);
        this.role = Objects.requireNonNull(role);
    }

    public boolean isAdmin() {
        return role.isAdmin();
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name.value();
    }

    public String getEmail() {
        return email;
    }

    public MemberRole getRole() {
        return role;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        Member member = (Member) object;
        return Objects.equals(id, member.id)
                && Objects.equals(name, member.name)
                && Objects.equals(email, member.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, email);
    }
}
