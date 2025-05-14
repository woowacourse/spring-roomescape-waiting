package roomescape.unit.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import roomescape.entity.Member;
import roomescape.entity.Reservation;
import roomescape.entity.ReservationTime;
import roomescape.entity.Theme;
import roomescape.global.Role;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class ReservationTest {

    @Test
    void 현재_날짜보다_과거_날짜이면_false를_반환한다() {
        //given
        Member member = new Member(0L, "Hula", "test@test.com", "test", Role.USER);
        LocalDate yesterday = LocalDate.now().minusDays(1);
        ReservationTime reservationTime = new ReservationTime(1L, LocalTime.now());
        Theme theme = new Theme(1L, "테마", "설명", "썸네일");

        Reservation reservation = new Reservation(member, yesterday, reservationTime, theme);

        //when
        boolean actual = reservation.isBefore(LocalDateTime.now());

        //then
        assertThat(actual).isTrue();
    }

    @Test
    void 현재_시간보다_과거_시간이면_false를_반환한다() {
        //given
        Member member = new Member(0L, "Hula", "test@test.com", "test", Role.USER);
        LocalDate today = LocalDate.now();
        ReservationTime reservationTime = new ReservationTime(1L, LocalTime.now().minusMinutes(10));
        Theme theme = new Theme(1L, "테마", "설명", "썸네일");

        Reservation reservation = new Reservation(member, today, reservationTime, theme);

        //when
        boolean actual = reservation.isBefore(LocalDateTime.now());

        //then
        assertThat(actual).isTrue();
    }
}

