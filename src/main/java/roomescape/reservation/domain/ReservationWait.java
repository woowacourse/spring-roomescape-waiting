package roomescape.reservation.domain;

import jakarta.persistence.Entity;
import lombok.NoArgsConstructor;
import roomescape.member.domain.Member;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.ReservationTime;

@Entity
@NoArgsConstructor
public class ReservationWait extends Reservation {

    // TODO : 상속 시, 부모 테이블에서 조회를 하면 자식 테이블의 데이터까지 조회되는 문제 발생

    public ReservationWait(
            final Long id,
            final Member member,
            final ReservationDate date,
            final ReservationTime time,
            final Theme theme
    ) {
        super(id, member, date, time, theme);
    }

    private static ReservationWait of(
            final Long id,
            final Member member,
            final ReservationDate date,
            final ReservationTime time,
            final Theme theme
    ) {
        return new ReservationWait(id, member, date, time, theme);
    }

    public static ReservationWait withId(
            final Long id,
            final Member member,
            final ReservationDate date,
            final ReservationTime time,
            final Theme theme
    ) {
        validate(member, date, time, theme);
        return of(id, member, date, time, theme);
    }

    public static ReservationWait withoutId(
            final Member member,
            final ReservationDate date,
            final ReservationTime time,
            final Theme theme
    ) {
        validatePast(date, time);
        return of(null, member, date, time, theme);
    }
}
