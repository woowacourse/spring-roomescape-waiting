package roomescape.model;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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

    public boolean ownBy(Member comparedMember) {
        return reservation.getMember().getId().equals(comparedMember.getId());
    }

}
