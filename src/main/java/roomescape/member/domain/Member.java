package roomescape.member.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

    @Enumerated(EnumType.STRING)
    private Role role;
    private String email;
    private String password;

    @OneToMany(mappedBy = "member")
    private Set<Reservation> reservations = new HashSet<>();

    public Member(final Long id, final String name, final Role role, final String email, final String password) {
        validateNameIsNull(name);
        validateRoleIsNull(role);
        validateEmailIsNull(email);
        validatePasswordIsNull(password);

        this.id = id;
        this.name = name;
        this.role = role;
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

    private void validateRoleIsNull(final Role role) {
        if (role == null) {
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
        return new Member(id, member.name, member.role, member.email, member.password);
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

    public Role getMemberRole() {
        return role;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public Role getRole() {
        return role;
    }

    public Set<Reservation> getReservations() {
        return reservations;
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
