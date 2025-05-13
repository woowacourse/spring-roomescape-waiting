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
    private MemberName memberName;
    private MemberEmail email;
    private String password;
    @Enumerated(value = EnumType.STRING)
    private Role role;

    public Member(final Long id, final MemberName memberName,
                  final MemberEmail email, final String password, final Role role) {
        validateNotNull(memberName, email, password, role);
        this.id = id;
        this.memberName = memberName;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public Member(final long id, final String name, final String email, final String password, final String role) {
        this(id, new MemberName(name), new MemberEmail(email), password, Role.of(role));
    }

    public Member() {

    }

    public static Member register(final MemberName memberName, final MemberEmail email, final String password) {
        return new Member(null, memberName, email, password, Role.USER);
    }

    public Member withId(final long id) {
        return new Member(id, memberName, email, password, Role.USER);
    }

    public boolean hasRole(final Role other) {
        return this.role == other;
    }

    private void validateNotNull(final MemberName memberName, final MemberEmail email,
                                 final String password, final Role role) {
        if (memberName == null) {
            throw new IllegalArgumentException("이름을 입력해야 합니다.");
        }
        if (email == null) {
            throw new IllegalArgumentException("이메일을 입력해야 합니다.");
        }
        if (password == null) {
            throw new IllegalArgumentException("비밀번호를 입력해야 합니다.");
        }
        if (role == null) {
            throw new IllegalArgumentException("권한을 입력해야 합니다.");
        }
    }

    public Long getId() {
        return id;
    }

    public MemberName getMemberName() {
        return memberName;
    }

    public MemberEmail getMemberEmail() {
        return email;
    }

    public String getName() {
        return memberName.getName();
    }

    public String getEmail() {
        return email.getEmail();
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
