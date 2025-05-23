package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.domain.entity.GameSchedule;
import roomescape.domain.entity.Member;
import roomescape.domain.entity.Reservation;
import roomescape.domain.entity.ReservationTime;
import roomescape.domain.entity.Theme;

class ReservationTest {

    @DisplayName("같은 날짜, 시간, 테마로 예약하면 중복 예약으로 판단한다.")
    @Test
    void isDuplicated() {
        // given
        Member member = Member.ofUser(1L, "member", "member@email.com", "password");
        Theme theme = Theme.of(1L, "theme", "description", "thumbnail");
        ReservationTime time = ReservationTime.of(1L, LocalTime.of(9, 0));
        LocalDate date = LocalDate.now().plusDays(1);

        GameSchedule gameSchedule1 = GameSchedule.of(1L, date, time, theme);
        GameSchedule gameSchedule2 = GameSchedule.of(2L, date, time, theme);

        Reservation reservation1 = Reservation.of(1L, member, gameSchedule1, ReservationStatus.RESERVED);
        Reservation reservation2 = Reservation.of(2L, member, gameSchedule2, ReservationStatus.RESERVED);

        // when
        boolean isDuplicated = reservation1.isDuplicated(reservation2);

        // then
        assertThat(isDuplicated).isTrue();
    }

    @DisplayName("다른 날짜, 시간, 테마로 예약하면 중복 예약이 아니다.")
    @Test
    void isNotDuplicated() {
        // given
        Member member = Member.ofUser(1L, "member", "member@email.com", "password");
        Theme theme1 = Theme.of(1L, "theme1", "description1", "thumbnail1");
        Theme theme2 = Theme.of(2L, "theme2", "description2", "thumbnail2");
        ReservationTime time1 = ReservationTime.of(1L, LocalTime.of(9, 0));
        ReservationTime time2 = ReservationTime.of(2L, LocalTime.of(10, 0));
        LocalDate date1 = LocalDate.now().plusDays(1);
        LocalDate date2 = LocalDate.now().plusDays(2);

        GameSchedule gameSchedule1 = GameSchedule.of(1L, date1, time1, theme1);
        GameSchedule gameSchedule2 = GameSchedule.of(2L, date2, time2, theme2);

        Reservation reservation1 = Reservation.of(1L, member, gameSchedule1, ReservationStatus.RESERVED);
        Reservation reservation2 = Reservation.of(2L, member, gameSchedule2, ReservationStatus.RESERVED);

        // when
        boolean isDuplicated = reservation1.isDuplicated(reservation2);

        // then
        assertThat(isDuplicated).isFalse();
    }
}
