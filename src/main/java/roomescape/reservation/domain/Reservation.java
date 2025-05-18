package roomescape.reservation.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import roomescape.common.exception.ReservationException;
import roomescape.member.domain.Member;
import roomescape.theme.domain.Theme;

@Getter
@ToString
@NoArgsConstructor
@Entity
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;

    private LocalDate date;

    @ManyToOne(fetch = FetchType.LAZY)
    private ReservationTime time;

    @ManyToOne(fetch = FetchType.LAZY)
    private Theme theme;

    public Reservation(final Long id, final Member member, final Theme theme, final LocalDate date,
                       final ReservationTime time) {
        validateDate(date);
        validateTime(time);
        validateTheme(theme);
        this.id = id;
        this.member = member;
        this.theme = theme;
        this.date = date;
        this.time = time;
    }

    public Reservation(final Member member, final LocalDate date, final ReservationTime time, final Theme theme) {
        validateDate(date);
        validateTime(time);
        validateTheme(theme);
        this.member = member;
        this.date = date;
        this.time = time;
        this.theme = theme;
    }

    private void validateDate(final LocalDate date) {
        if (date == null) {
            throw new ReservationException("Date cannot be null");
        }
    }

    private void validateTime(final ReservationTime time) {
        if (time == null) {
            throw new ReservationException("Time cannot be null");
        }
    }

    private void validateTheme(final Theme theme) {
        if (theme == null) {
            throw new ReservationException("Theme cannot be null");
        }
    }
}
