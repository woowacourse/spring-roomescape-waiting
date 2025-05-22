package roomescape.reservation.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberName;
import roomescape.theme.domain.Theme;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "id")
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate date;

    @ManyToOne(fetch = FetchType.LAZY)
    private ReservationTime time;

    @ManyToOne(fetch = FetchType.LAZY)
    private Theme theme;

    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;

    public Reservation(
            final Long id,
            final LocalDate date,
            final ReservationTime time,
            final Theme theme,
            final Member member
    ) {
        this.id = id;
        this.date = date;
        this.time = time;
        this.theme = theme;
        this.member = member;
    }

    public Reservation(
            final LocalDate date,
            final ReservationTime time,
            final Theme theme,
            final Member member
    ) {
        this(null, date, time, theme, member);
    }

    public boolean hasConflictWith(final ReservationTime reservationTime, final Theme theme) {
        final LocalTime startAt = time.getStartAt();
        return this.theme.equals(theme) &&
                reservationTime.hasConflict(theme.getDuration(), startAt);
    }

    public MemberName getName() {
        return member.getName();
    }

    public String getThemeName() {
        return theme.getName();
    }

    public LocalTime getStartAt() {
        return time.getStartAt();
    }
}
