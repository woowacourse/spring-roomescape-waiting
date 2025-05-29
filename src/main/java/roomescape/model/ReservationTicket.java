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
    private ReservationSpec reservationSpec;

    public ReservationTicket(ReservationSpec reservationSpec) {
        this.reservationSpec = reservationSpec;
    }

    public Member getMember() {
        return reservationSpec.getMember();
    }

    public LocalDate getDate() {
        return reservationSpec.getDate();
    }

    public ReservationTime getReservationTime() {
        return reservationSpec.getReservationTime();
    }

    public Theme getTheme() {
        return reservationSpec.getTheme();
    }

    public boolean ownBy(Member comparedMember) {
        return reservationSpec.getMember().getId().equals(comparedMember.getId());
    }

}
