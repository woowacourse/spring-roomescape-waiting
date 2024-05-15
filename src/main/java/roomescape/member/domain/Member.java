package roomescape.member.domain;

import jakarta.persistence.*;
import roomescape.reservation.domain.Reservation;

import java.util.Objects;
import java.util.Set;

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
    private Password password;

    @OneToMany(mappedBy = "member")
    private Set<Reservation> reservations;

    private Member() {
    }

    public Member(Long id, Name name, Email email, Role role, Password password) {
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

    public boolean isAdmin() {
        return role == Role.ADMIN;
    }

    public boolean isPassword(Password password) {
        return Objects.equals(this.password, password);
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
