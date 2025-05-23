package roomescape.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Waiting {

    @Getter
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    @Getter
    @Column(nullable = false)
    private LocalDate date;

    @Getter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Theme theme;

    @Getter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private ReservationTime reservationTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Member member;

    private Waiting(final LocalDate date, final Theme theme, final ReservationTime reservationTime,
                    final Member member) {
        this.date = date;
        this.theme = theme;
        this.reservationTime = reservationTime;
        this.member = member;
    }

    public static Waiting of(final LocalDate date, final Theme theme, final ReservationTime reservationTime,
                             final Member member) {
        return new Waiting(date, theme, reservationTime, member);
    }

    public void isAfterBy(final LocalDate now) {
        if (!this.date.isAfter(now)) {
            throw new IllegalStateException("과거 및 당일 예약은 대기 신청이 불가능합니다.");
        }
    }

}
