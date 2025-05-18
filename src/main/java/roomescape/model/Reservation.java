package roomescape.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate date;

    @Setter
    @ManyToOne
    private ReservationTime reservationTime;

    @Setter
    @ManyToOne
    private Theme theme;

    @Setter
    @ManyToOne
    private Member member;

    public Reservation(LocalDate date, ReservationTime reservationTime, Theme theme, Member member, LocalDate today) {
        this.date = date;
        this.reservationTime = reservationTime;
        this.theme = theme;
        this.member = member;

        validateReservationDateInFuture(today);
    }

    private void validateReservationDateInFuture(LocalDate today) {
        if (!this.date.isAfter(today)) {
            throw new IllegalStateException("과거 및 당일 예약은 불가능합니다.");
        }
    }
}
