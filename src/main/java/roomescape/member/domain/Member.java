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

    @OneToMany(mappedBy = "member", fetch = FetchType.LAZY)
    private List<Reservation> reservations = new ArrayList<>();

    public Member() {
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

    public void addReservation(final Reservation reservation) {
        reservations.add(reservation);
    }

    public boolean isSamePassword(final String password) {
        return this.password.getPassword().equals(password);
    }

    public Long getId() {
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

    public List<Reservation> getReservations() {
        return reservations;
    }
}
