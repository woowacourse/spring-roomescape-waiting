package roomescape.member.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import roomescape.reservation.model.Reservation;

@Entity
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private MemberRole memberRole;
    private String email;
    private String password;

    @OneToMany(mappedBy = "member")
    private Set<Reservation> reservation = new HashSet<>();

    public Member(final Long id, final String name, final MemberRole memberRole, final String email, final String password) {
        validateNameIsNull(name);
        validateRoleIsNull(memberRole);
        validateEmailIsNull(email);
        validatePasswordIsNull(password);

        this.id = id;
        this.name = name;
        this.memberRole = memberRole;
        this.email = email;
        this.password = password;
    }

    protected Member() {

    }

    private void validateNameIsNull(final String name) {
        if (name == null) {
            throw new IllegalArgumentException("회원 생성 시 이름은 필수입니다.");
        }
    }

    private void validateRoleIsNull(final MemberRole memberRole) {
        if (memberRole == null) {
            throw new IllegalArgumentException("회원 생성 시 회원 권한 지정은 필수입니다.");
        }
    }

    private void validateEmailIsNull(final String email) {
        if (email == null) {
            throw new IllegalArgumentException("회원 생성 시 이메일 필수입니다.");
        }
    }

    private void validatePasswordIsNull(final String password) {
        if (password == null) {
            throw new IllegalArgumentException("회원 생성 시 비밀번호는 필수입니다.");
        }
    }

    public static Member of(final Long id, final Member member) {
        return new Member(id, member.name, member.memberRole, member.email, member.password);
    }

    public boolean hasNotSamePassword(final String password) {
        return !this.password.equals(password);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public MemberRole getMemberRole() {
        return memberRole;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Member member = (Member) o;
        return Objects.equals(id, member.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
