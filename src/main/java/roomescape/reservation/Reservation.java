package roomescape.reservation;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import roomescape.exception.custom.reason.reservation.ReservationPastTimeException;
import roomescape.member.Member;
import roomescape.reservationtime.ReservationTime;
import roomescape.theme.Theme;

@Entity
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private final Long id;

    @Embedded
    private ReservationDate date;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private ReservationTime reservationTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Theme theme;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationStatus reservationStatus;

    protected Reservation() {
        id = null;
    }

    public static Reservation of(
            final ReservationDate date,
            final Member member,
            final ReservationTime reservationTime,
            final Theme theme,
            final ReservationStatus reservationStatus,
            final LocalDateTime currentDateTime
            ) {
        validatePastTime(date, reservationTime, currentDateTime);

        return new Reservation(null, date, member, reservationTime, theme, reservationStatus);
    }

    private static void validatePastTime(final ReservationDate date, final ReservationTime reservationTime,
                                  final LocalDateTime currentDateTime) {
        if(date.isToday(currentDateTime.toLocalDate())
                && reservationTime.isBefore(currentDateTime.toLocalTime())){
            throw new ReservationPastTimeException();
        }
    }

    public boolean isWaiting() {
        return reservationStatus == ReservationStatus.WAITING;
    }

    public void pending(){
        this.reservationStatus = ReservationStatus.PENDING;
    }
}
