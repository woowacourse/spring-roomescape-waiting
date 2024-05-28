package roomescape.member.domain;

import jakarta.persistence.*;
import roomescape.exceptions.MissingRequiredFieldException;
import roomescape.reservation.domain.ReservationTime;
import roomescape.theme.domain.Theme;

import java.time.LocalDate;
import java.util.Objects;

@Entity
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    private Name name;

    @Embedded
    private Email email;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Embedded
    @AttributeOverride(name = "encodedPassword", column = @Column(name = "password"))
    private Password password;

    protected Member() {
    }

    public Member(Long id, Name name, Email email, Role role, Password password) {
        validateNotNull(name, email, role, password);
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
        this.password = password;
    }

    public Member(Name name, Email email, Role role, Password password) {
        this(null, name, email, role, password);
    }

    public Member(Long id, String name, String email, String role, String password) {
        this(id, new Name(name), new Email(email), Role.valueOf(role), new Password(password));
    }

    private void validateNotNull(Name name, Email email, Role role, Password password) {
        if (name == null) {
            throw new MissingRequiredFieldException(LocalDate.class.getSimpleName() + "값이 null 입니다.");
        }
        if (email == null) {
            throw new MissingRequiredFieldException(ReservationTime.class.getSimpleName() + "값이 null 입니다.");
        }
        if (role == null) {
            throw new MissingRequiredFieldException(Theme.class.getSimpleName() + "값이 null 입니다.");
        }
        if (password == null) {
            throw new MissingRequiredFieldException(Member.class.getSimpleName() + "값이 null 입니다.");
        }
    }

    public boolean isAdmin() {
        return role == Role.ADMIN;
    }

    public Long getId() {
        return id;
    }

    public Name getName() {
        return name;
    }

    public Email getEmail() {
        return email;
    }

    public Role getRole() {
        return role;
    }

    public Password getPassword() {
        return password;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Member that = (Member) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
