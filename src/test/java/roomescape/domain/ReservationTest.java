package roomescape.domain;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.domain.member.Member;
import roomescape.domain.member.Role;

import java.time.LocalDate;
import java.time.LocalTime;

class ReservationTest {

    @DisplayName("Date 이 존재하지 않으면 생성 불가능하다")
    @Test
    void invalidReservationDateTimeTest() {
        Member member = new Member(1L, "가이온", "hello@woowa.com", Role.USER, "password");
        Assertions.assertThatThrownBy(() ->
                        new Reservation(1L, member, null, new ReservationTime(1L, LocalTime.now()), new Theme(1L, "우테코", "방탈출", ".png")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("ReservationTime이 존재하지 않으면 생성 불가능하다")
    @Test
    void invalidReservationTimeTest() {
        Member member = new Member(1L, "가이온", "hello@woowa.com", Role.USER, "password");
        Assertions.assertThatThrownBy(() ->
                        new Reservation(1L, member, LocalDate.now(), null, new Theme(1L, "우테코", "방탈출", ".png")))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
