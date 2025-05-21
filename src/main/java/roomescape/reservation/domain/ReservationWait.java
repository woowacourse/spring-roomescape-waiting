package roomescape.reservation.domain;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import roomescape.member.domain.Member;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.ReservationTime;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "id")
public class ReservationWait {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    private ReservationInfo info;

    private static ReservationWait of(Long id, ReservationInfo info) {
        return new ReservationWait(id, info);
    }

    public static ReservationWait withId(
            Long id,
            Member member,
            ReservationDate date,
            ReservationTime time,
            Theme theme
    ) {
        return of(id, ReservationInfo.of(member, date, time, theme));
    }

    public static ReservationWait withoutId(
            Member member,
            ReservationDate date,
            ReservationTime time,
            Theme theme
    ) {
        Reservation.validatePast(date, time);
        final ReservationInfo info = ReservationInfo.of(member, date, time, theme);
        return of(null, info);
    }

    public Member getMember() {
        return info.getMember();
    }

    public ReservationDate getDate() {
        return info.getDate();
    }

    public ReservationTime getTime() {
        return info.getTime();
    }

    public Theme getTheme() {
        return info.getTheme();
    }
}
