package roomescape.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate date;

    @ManyToOne
    private ReservationTime reservationTime;

    @ManyToOne
    private Theme theme;

    @ManyToOne
    private Member member;

    public Reservation(LocalDate date, ReservationTime reservationTime, Theme theme, Member member, LocalDate today) {
        validateReservationDateInFuture(today);

        this.date = date;
        this.reservationTime = reservationTime;
        this.theme = theme;
        this.member = member;
    }

    private void validateReservationDateInFuture(LocalDate today) {
        if (!this.date.isAfter(today)) {
            throw new IllegalStateException("과거 및 당일 예약은 불가능합니다.");
        }
    }
}
