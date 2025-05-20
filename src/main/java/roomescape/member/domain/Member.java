package roomescape.member.domain;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;
import roomescape.reservation.domain.Reservation;

@Entity
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    private Name name;
    @Embedded
    private Email email;
    @Embedded
    private Password password;

    @OneToMany(mappedBy = "member")
    private List<Reservation> reservations = new ArrayList<>();

    protected Member() {
    }

    private Member(final Long id, final Name name, final Email email, final Password password) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
    }

    public static Member createWithoutId(String name, String email, String password) {
        return new Member(null, new Name(name), new Email(email), new Password(password));
    }

    public boolean isSamePassword(final String password) {
        String storedPassword = this.password.password();
        return storedPassword.equals(password);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name.name();
    }

    public String getEmail() {
        return email.email();
    }

    public String getPassword() {
        return password.password();
    }

    public List<Reservation> getReservations() {
        return reservations;
    }
}
