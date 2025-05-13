package roomescape.reservation;

import jakarta.persistence.Entity;
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
    private final LocalDate date;

    @ManyToOne
    private final Member member;

    @ManyToOne
    private final ReservationTime reservationTime;

    @ManyToOne
    private final Theme theme;

    public Reservation() {
        this(null, null, null, null, null);
    }

    public Reservation(
            final LocalDate date,
            final Member member,
            final ReservationTime reservationTime,
            final Theme theme
    ) {
        this(null, date, member, reservationTime, theme);
    }
}
