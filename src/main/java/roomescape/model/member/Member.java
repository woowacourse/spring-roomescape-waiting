package roomescape.model.member;

import jakarta.persistence.*;
import jakarta.validation.Valid;
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
    @Valid
    @Embedded
    private MemberName name;
    @NotNull
    @Valid
    @Embedded
    private MemberEmail email;
    @NotNull
    @Valid
    @Embedded
    private MemberPassword password;
    @NotNull
    @Enumerated(EnumType.STRING)
    private Role role;
    @OneToMany(mappedBy = "member")
    private Set<Reservation> reservations;

    public Member(long id, String name, String email, String password, Role role) {
        this.id = id;
        this.name = new MemberName(name);
        this.email = new MemberEmail(email);
        this.password = new MemberPassword(password);
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
        return id == member.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, email, password, role);
    }
}
