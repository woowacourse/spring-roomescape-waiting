package roomescape.model;

import jakarta.persistence.AssociationOverride;
import jakarta.persistence.AssociationOverrides;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class ReservationTicket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    @AssociationOverrides({
            @AssociationOverride(name = "reservationTime", joinColumns = @JoinColumn(name = "reservation_time_id")),
            @AssociationOverride(name = "theme", joinColumns = @JoinColumn(name = "theme_id")),
            @AssociationOverride(name = "member", joinColumns = @JoinColumn(name = "member_id"))
    })
    private Reservation reservation;

    public ReservationTicket(Reservation reservation) {
        this.reservation = reservation;
    }

    public Member getMember() {
        return reservation.getMember();
    }

    public LocalDate getDate() {
        return reservation.getDate();
    }

    public ReservationTime getReservationTime() {
        return reservation.getReservationTime();
    }

    public Theme getTheme() {
        return reservation.getTheme();
    }
}
