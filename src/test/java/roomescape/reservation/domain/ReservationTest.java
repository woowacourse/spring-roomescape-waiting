package roomescape.reservation.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;
import roomescape.theme.domain.Theme;


class ReservationTest {

    @Test
    void 해당_예약시간과_테마를_사용할_수_있는지_확인한다() {
        // given
        final ReservationTime eight = new ReservationTime(1L, LocalTime.of(8, 0));
        final ReservationTime nine = new ReservationTime(2L, LocalTime.of(9, 0));
        final ReservationTime ten = new ReservationTime(3L, LocalTime.of(10, 0));
        final ReservationTime eleven = new ReservationTime(4L, LocalTime.of(11, 0));
        final ReservationTime twelve = new ReservationTime(5L, LocalTime.of(12, 0));

        final Theme theme = new Theme(1L, "인터스텔라", "설명1", "썸네일1");
        final Reservation reservation = new Reservation(1L, LocalDate.of(2025, 1, 1), ten, theme,
                new Member("엠제이", "", "", Role.MEMBER), Status.CONFIRMATION);

        // 현재 테마 이용시간은 2시간으로 고정됨
        // 10시로 예약을 했으니, 8시 초과 12시 미만일 때는 예약을 할 수 없음
        // when & then
        assertAll(
                () -> assertThat(reservation.hasConflictWith(eight, theme)).isFalse(),
                () -> assertThat(reservation.hasConflictWith(nine, theme)).isTrue(),
                () -> assertThat(reservation.hasConflictWith(eleven, theme)).isTrue(),
                () -> assertThat(reservation.hasConflictWith(twelve, theme)).isFalse()
        );

    }
}
