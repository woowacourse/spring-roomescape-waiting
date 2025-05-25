package roomescape.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
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

    @Column(nullable = false)
    private LocalDate date;

    @ManyToOne
    private ReservationTime reservationTime;

    @ManyToOne
    private Theme theme;

    @ManyToOne
    private Member member;

    public ReservationTicket(LocalDate date, ReservationTime reservationTime, Theme theme, Member member,
                             LocalDate today) {
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
