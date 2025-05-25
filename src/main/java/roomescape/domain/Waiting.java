package roomescape.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Waiting {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    @Column(nullable = false)
    private LocalDate date;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Theme theme;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private ReservationTime reservationTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Member member;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private Waiting(final LocalDate date, final Theme theme, final ReservationTime reservationTime, final Member member,
                    final LocalDateTime createdAt) {
        this.date = date;
        this.theme = theme;
        this.reservationTime = reservationTime;
        this.member = member;
        this.createdAt = createdAt;
    }

    public static Waiting of(final LocalDate date, final Theme theme, final ReservationTime reservationTime,
                             final Member member, final LocalDateTime createdAt) {
        return new Waiting(date, theme, reservationTime, member, createdAt);
    }

    public void isAfterBy(final LocalDate reservationDate) {
        if (isBefore(reservationDate)) {
            throw new IllegalStateException("과거 및 당일 예약은 대기 신청이 불가능합니다.");
        }
    }

    private boolean isBefore(final LocalDate reservationDate) {
        return !this.date.isAfter(reservationDate);
    }

}
