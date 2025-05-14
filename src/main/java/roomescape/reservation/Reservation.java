package roomescape.reservation;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import roomescape.member.Member;
import roomescape.reservationtime.ReservationTime;
import roomescape.theme.Theme;

@Entity
@Getter
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private final Long id;
    private LocalDate date;

    @ManyToOne
    private Member member;

    @ManyToOne
    private ReservationTime reservationTime;

    @ManyToOne
    private Theme theme;

    @Enumerated(EnumType.STRING)
    private ReservationStatus reservationStatus;

    public Reservation() {
        id = null;
    }

    public Reservation(
            final LocalDate date,
            final Member member,
            final ReservationTime reservationTime,
            final Theme theme,
            final ReservationStatus reservationStatus
    ) {
        this(null, date, member, reservationTime, theme, reservationStatus);
    }
}
