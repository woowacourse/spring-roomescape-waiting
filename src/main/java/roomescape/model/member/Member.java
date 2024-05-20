package roomescape.model.member;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.NotNull;
import java.util.Objects;
import java.util.Set;
import roomescape.model.Reservation;

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
    @OneToMany(mappedBy = "member", fetch = FetchType.LAZY)
    private Set<Reservation> reservations;

    public Member(long id, String name, String email, String password, Role role) {
        this.id = id;
        this.name = new Name(name);
        this.email = new Email(email);
        this.password = new Password(password);
        this.role = role;
    }

    public Member() {
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name.getName();
    }

    public String getEmail() {
        return email.getEmail();
    }

    public String getPassword() {
        return password.getPassword();
    }

    public Role getRole() {
        return role;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Member member = (Member) o;
        return id == member.id
                && Objects.equals(name, member.name)
                && Objects.equals(email, member.email)
                && Objects.equals(password, member.password)
                && role == member.role;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, email, password, role);
    }
}
