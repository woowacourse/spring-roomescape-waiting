package roomescape.reservation.domain.reservation;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.theme.Theme;
import roomescape.reservation.domain.time.ReservationTime;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Accessors(fluent = true)
@EqualsAndHashCode(of = "id")
public class Reservation {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate date;

    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    private ReservationTime time;

    @ManyToOne(fetch = FetchType.LAZY)
    private Theme theme;

    public Reservation(final LocalDate date, final Member member, final ReservationTime time, final Theme theme) {
        this(null, date, member, time, theme);
    }

    public boolean isSameTime(final ReservationTime time) {
        return this.time.equals(time);
    }

    public boolean isMemberSameId(final long id) {
        return member.isSameId(id);
    }

    public boolean isThemeSameId(final long id) {
        return theme.isSameId(id);
    }

    public boolean isDateBetween(final LocalDate from, final LocalDate to) {
        return (date.isEqual(from) || date.isAfter(from)) &&
                (date.isEqual(to) || date.isBefore(to));
    }
}
