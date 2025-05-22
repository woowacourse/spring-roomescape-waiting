package roomescape.unit.reservation.domain;

import java.time.LocalDate;
import java.time.LocalTime;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import roomescape.exception.ArgumentNullException;
import roomescape.exception.PastDateTimeReservationException;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.Theme;
import roomescape.reservation.domain.TimeSlot;

class ReservationTest {

    @Test
    void 예약자명이_null일_경우_예외가_발생한다() {
        // given
        TimeSlot timeSlot = TimeSlot.builder()
                .startAt(LocalTime.of(9, 0)).build();
        Theme theme = Theme.builder()
                .name("themeName")
                .description("des")
                .thumbnail("th").build();
        // when & then
        Assertions.assertThatThrownBy(
                        () -> Reservation.builder()
                                .date(LocalDate.now().plusDays(1))
                                .timeSlot(timeSlot)
                                .theme(theme).build())
                .isInstanceOf(ArgumentNullException.class);

    }

    @Test
    void 예약날짜가_null일_경우_예외가_발생한다() {
        // given
        Member member = Member.builder()
                .name("name1")
                .email("email1@domain.com")
                .password("password1").build();
        TimeSlot timeSlot = TimeSlot.builder()
                .startAt(LocalTime.of(9, 0)).build();
        Theme theme = Theme.builder()
                .name("themeName")
                .description("des")
                .thumbnail("th").build();
        // when & then
        Assertions.assertThatThrownBy(
                        () -> Reservation.builder()
                                .member(member)
                                .timeSlot(timeSlot)
                                .theme(theme).build())
                .isInstanceOf(ArgumentNullException.class);
    }

    @Test
    void 예약시간이_null일_경우_예외가_발생한다() {
        // given
        Member member = Member.builder()
                .name("name1")
                .email("email1@domain.com")
                .password("password1").build();
        Theme theme = Theme.builder()
                .name("themeName")
                .description("des")
                .thumbnail("th").build();
        // when & then
        Assertions.assertThatThrownBy(
                        () -> Reservation.builder()
                                .member(member)
                                .theme(theme)
                                .date(LocalDate.of(2025, 1, 1)).build()
                )
                .isInstanceOf(ArgumentNullException.class);
    }

    @Test
    void 지나간_시간에_예약을_생성할_경우_예외가_발생한다() {
        // given
        Member member = Member.builder()
                .name("name1")
                .email("email1@domain.com")
                .password("password1").build();
        TimeSlot timeSlot = TimeSlot.builder()
                .id(1L)
                .startAt(LocalTime.of(9, 0)).build();
        Theme theme = Theme.builder()
                .name("themeName")
                .description("des")
                .thumbnail("th").build();
        // when & then
        Reservation reservation = Reservation.builder()
                .member(member)
                .date(LocalDate.of(2024, 1, 1))
                .timeSlot(timeSlot)
                .theme(theme).build();
        Assertions.assertThatThrownBy(() -> reservation.validateDateTime())
                .isInstanceOf(PastDateTimeReservationException.class);
    }

    @Test
    void 테마가_null일_경우_예외가_발생한다() {
        // given
        Member member = Member.builder()
                .name("name1")
                .email("email1@domain.com")
                .password("password1").build();
        TimeSlot timeSlot = TimeSlot.builder()
                .id(1L)
                .startAt(LocalTime.of(9, 0)).build();
        // when & then
        Assertions.assertThatThrownBy(
                        () -> Reservation.builder()
                                .member(member)
                                .date(LocalDate.of(2024, 1, 1))
                                .timeSlot(timeSlot).build())
                .isInstanceOf(ArgumentNullException.class);

    }
}