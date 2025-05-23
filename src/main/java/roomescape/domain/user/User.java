package roomescape.domain.user;

import jakarta.persistence.CascadeType;
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
import roomescape.domain.reservation.ReservationSlot;
import roomescape.exception.AlreadyExistedException;
import roomescape.exception.NotFoundException;

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
    @OneToMany(mappedBy = "user", cascade = {CascadeType.ALL}, orphanRemoval = true)
    private List<Reservation> reservations = new ArrayList<>();

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

    public List<ReservationSlot> reservedSlots() {
        return reservations.stream()
            .map(Reservation::slot)
            .toList();
    }

    public void reserve(final Reservation reservation) {
        if (alreadyReservedAt(reservation.slot())) {
            throw new AlreadyExistedException("이미 해당 예약 슬롯에 예약 또는 대기하셨습니다.");
        }
        reservations.add(reservation);
    }

    private boolean alreadyReservedAt(final ReservationSlot slot) {
        return reservedSlots().contains(slot);
    }

    public void cancelReservation(final Reservation reservation) {
        if (!reservations.contains(reservation)) {
            throw new NotFoundException("해당 예약 내역이 존재하지 않습니다.");
        }

        reservation.cancel();
        reservations.remove(reservation);
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
