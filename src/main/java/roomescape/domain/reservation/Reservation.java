package roomescape.domain.reservation;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import roomescape.domain.member.Member;
import roomescape.domain.reservationdate.ReservationDate;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.Theme;
import roomescape.support.exception.MemberErrorCode;
import roomescape.support.exception.ReservationErrorCode;
import roomescape.support.exception.ReservationTimeErrorCode;
import roomescape.support.exception.RoomescapeException;
import roomescape.support.exception.ThemeErrorCode;

@Entity
@Table(uniqueConstraints = {
    @UniqueConstraint(columnNames = {"date_id", "time_id", "theme_id"})
})
@Getter
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "date_id")
    private ReservationDate date;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "time_id")
    private ReservationTime time;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "theme_id")
    private Theme theme;

    protected Reservation() {
    }

    private Reservation(Long id, Member member, ReservationDate date, ReservationTime time, Theme theme) {
        validate(member, date, time, theme);
        this.id = id;
        this.member = member;
        this.date = date;
        this.time = time;
        this.theme = theme;
    }

    private Reservation(Member member, ReservationDate date, ReservationTime time, Theme theme) {
        this(null, member, date, time, theme);
    }

    public static Reservation createWithoutId(Member member, ReservationDate date, ReservationTime time, Theme theme) {
        return new Reservation(member, date, time, theme);
    }

    public static Reservation of(Long id, Member member, ReservationDate date, ReservationTime time, Theme theme) {
        return new Reservation(id, member, date, time, theme);
    }

    public void update(ReservationDate date, ReservationTime time) {
        this.date = date;
        this.time = time;
    }

    private static void validate(Member member, ReservationDate date, ReservationTime time, Theme theme) {
        if (member == null) {
            throw new RoomescapeException(MemberErrorCode.INVALID_MEMBER);
        }
        if (date == null) {
            throw new RoomescapeException(ReservationErrorCode.INVALID_RESERVATION_DATE);
        }
        if (time == null) {
            throw new RoomescapeException(ReservationTimeErrorCode.INVALID_RESERVATION_TIME);
        }
        if (theme == null) {
            throw new RoomescapeException(ThemeErrorCode.INVALID_THEME);
        }
    }
}
