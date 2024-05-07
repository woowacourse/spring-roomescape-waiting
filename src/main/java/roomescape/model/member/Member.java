package roomescape.model.member;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import roomescape.model.Reservation;

import java.util.Objects;
import java.util.Set;

@Entity
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @NotNull
    @Embedded
    private Name name;
    @NotNull
    @Embedded
    private Email email;
    @NotNull
    @Embedded
    private Password password;
    @NotNull
    @Enumerated(EnumType.STRING)
    private Role role;
    @OneToMany(mappedBy = "member")
    private Set<Reservation> reservations;

    public Member(long id, String name, String email, String password, Role role) {
        this.id = id;
        this.name = new Name(name);
        this.email = new Email(email);
        this.password = new Password(password);
        this.role = role;
    }

    public Member(String name, String email, String password, Role role) {
        this(0, name, email, password, role);
    }

    public Member() {
    }

    public long getId() {
        return id;
    }

    public Name getName() {
        return name;
    }

    public Email getEmail() {
        return email;
    }

    public Password getPassword() {
        return password;
    }

    public Role getRole() {
        return role;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Member member = (Member) o;
        return id == member.id
                && Objects.equals(name.getName(), member.name.getName())
                && Objects.equals(email.getEmail(), member.email.getEmail())
                && Objects.equals(password.getPassword(), member.password.getPassword())
                && role == member.role;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, email, password, role);
    }
}
