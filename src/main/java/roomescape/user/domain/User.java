package roomescape.user.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import java.util.HashSet;
import java.util.Set;
import roomescape.reservation.domain.Reservation;

@Entity
public class User {

    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    private final Long id;
    private final Role role;
    private final String name;
    private final String email;
    private final String password;

    @OneToMany(mappedBy = "user")
    private final Set<Reservation> reservations;

    public User(Long id, Role role, String name, String email, String password) {
        this.id = id;
        this.role = role;
        this.name = name;
        this.email = email;
        this.password = password;
        this.reservations = new HashSet<>();
    }

    public User(String roleName, String name, String email, String password) {
        this(null, Role.findByName(roleName), name, email, password);
    }

    public boolean isMember() {
        return this.role.equals(Role.ROLE_MEMBER);
    }

    public Long getId() {
        return id;
    }

    public Role getRole() {
        return role;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof User that)) {
            return false;
        }
        if (this.id == null || that.id == null) {
            return false;
        }
        return this.id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return (id != null) ? id.hashCode() : 0;
    }
}
