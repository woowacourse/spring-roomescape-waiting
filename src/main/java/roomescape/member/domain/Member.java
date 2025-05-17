package roomescape.member.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.util.Objects;

@Entity
public final class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private MemberName name;
    private MemberEmail email;
    private String password;
    @Enumerated(value = EnumType.STRING)
    private Role role;

    public Member(final Long id, final MemberName name,
                  final MemberEmail email, final String password, final Role role) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public static Member register(final MemberName name, final MemberEmail email, final String password) {
        return new Member(null, name, email, password, Role.USER);
    }

    protected Member() {

    }

    public boolean hasRole(final Role other) {
        return this.role == other;
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

    public String getPassword() {
        return password;
    }

    public Role getRole() {
        return role;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (this.id == null || ((Member) o).id == null) {
            return false;
        }

        final Member member = (Member) o;
        return Objects.equals(id, member.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
