package roomescape.reservation;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import roomescape.member.Member;
import roomescape.reservationtime.ReservationTime;
import roomescape.theme.Theme;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;
    private LocalDate date;

    @ManyToOne
    private Member member;

    @ManyToOne
    private ReservationTime reservationTime;

    @ManyToOne
    private Theme theme;

    @Enumerated(EnumType.STRING)
    private ReservationStatus reservationStatus;

    public Reservation(
            final LocalDate date,
            final Member member,
            final ReservationTime reservationTime,
            final Theme theme,
            final ReservationStatus reservationStatus
    ) {
        this.date = date;
        this.member = member;
        this.reservationTime = reservationTime;
        this.theme = theme;
        this.reservationStatus = reservationStatus;
    }
}
