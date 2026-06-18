package roomescape.domain.waitingreservation;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.Getter;
import roomescape.domain.member.Member;
import roomescape.domain.reservationdate.ReservationDate;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.Theme;
import roomescape.support.exception.MemberErrorCode;
import roomescape.support.exception.RoomescapeException;
import roomescape.support.exception.WaitingReservationErrorCode;

@Entity
@Table(uniqueConstraints = {
    @UniqueConstraint(columnNames = {"member_id", "date_id", "time_id", "theme_id"})
})
@Getter
public class WaitingReservation {

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

    private LocalDateTime createdAt;

    protected WaitingReservation() {
    }

    private WaitingReservation(Long id, Member member, ReservationDate date, ReservationTime time, Theme theme,
        LocalDateTime createdAt) {
        validate(member, createdAt);
        this.id = id;
        this.member = member;
        this.date = date;
        this.time = time;
        this.theme = theme;
        this.createdAt = createdAt;
    }

    public static WaitingReservation createWithoutId(Member member, ReservationDate date, ReservationTime time,
        Theme theme, LocalDateTime createdAt) {
        return new WaitingReservation(null, member, date, time, theme, createdAt);
    }

    public static WaitingReservation of(Long id, Member member, ReservationDate date, ReservationTime time,
        Theme theme, LocalDateTime createdAt) {
        return new WaitingReservation(id, member, date, time, theme, createdAt);
    }

    private static void validate(Member member, LocalDateTime createdAt) {
        if (member == null) {
            throw new RoomescapeException(MemberErrorCode.INVALID_MEMBER);
        }
        if (createdAt == null) {
            throw new RoomescapeException(WaitingReservationErrorCode.INVALID_CREATED_AT);
        }
    }
}
