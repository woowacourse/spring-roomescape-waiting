package roomescape.domain.user;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import roomescape.domain.reservation.Reservation;

@EqualsAndHashCode(of = {"id"})
@Getter
@Accessors(fluent = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity(name = "USERS")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Embedded
    private UserName name;
    @Enumerated(EnumType.STRING)
    private UserRole role;
    @Embedded
    private Email email;
    @Embedded
    private Password password;
    @OneToMany(mappedBy = "user")
    private final List<Reservation> reservations = new ArrayList<>();

    public User(final long id, final UserName name, final UserRole role, final Email email,
        final Password password) {
        this.id = id;
        this.name = name;
        this.role = role;
        this.email = email;
        this.password = password;
    }

    public User(final UserName name, final Email email, final Password password) {
        this(0L, name, UserRole.USER, email, password);
    }

    public boolean matchesPassword(final Password passwordToCompare) {
        return password.matches(passwordToCompare);
    }

    public boolean isAdmin() {
        return role == UserRole.ADMIN;
    }

    public List<Reservation> reservations() {
        return List.copyOf(reservations);
    }

    @Override
    public String toString() {
        return "User{" +
               "id=" + id +
               ", name=" + name.value() +
               ", role=" + role +
               ", email=" + email.value() +
               '}';
    }
}
