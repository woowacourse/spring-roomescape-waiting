package roomescape.reservation.domain;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import roomescape.common.exception.BadRequestException;
import roomescape.member.domain.Member;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.ReservationTime;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "id")
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    private ReservationInfo info;

    private static Reservation of(Long id, ReservationInfo info) {
        return new Reservation(id, info);
    }

    public static Reservation withId(
            Long id,
            Member member,
            ReservationDate date,
            ReservationTime time,
            Theme theme
    ) {
        return of(id, ReservationInfo.of(member, date, time, theme));
    }

    public static Reservation withoutId(
            Member member,
            ReservationDate date,
            ReservationTime time,
            Theme theme
    ) {
        validatePast(date, time);
        final ReservationInfo info = ReservationInfo.of(member, date, time, theme);
        return of(null, info);
    }

    public static void validatePast(final ReservationDate date, final ReservationTime time) {
        final LocalDateTime now = LocalDateTime.now();
        if (date.isAfter(now.toLocalDate())) {
            return;
        }
        if (date.isBefore(now.toLocalDate())) {
            throw new BadRequestException("지난 날짜는 예약할 수 없습니다.");
        }
        if (time.isBefore(now.toLocalTime())) {
            throw new BadRequestException("이미 지난 시간에는 예약할 수 없습니다.");
        }
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
