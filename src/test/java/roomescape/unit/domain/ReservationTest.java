package roomescape.unit.domain;

import java.time.LocalDate;
import java.time.LocalTime;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import roomescape.auth.Role;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.exception.ArgumentNullException;
import roomescape.exception.PastDateTimeReservationException;

class ReservationTest {

    @Test
    void 예약자명이_null일_경우_예외가_발생한다() {
        // when & then
        Assertions.assertThatThrownBy(
                        () -> Reservation.createWithoutId(
                                null,
                                LocalDate.now().plusDays(1),
                                ReservationTime.createWithoutId(LocalTime.of(9, 0)),
                                Theme.createWithoutId("themeName", "des", "th")
                        ))
                .isInstanceOf(ArgumentNullException.class);

    }

    @Test
    void 예약날짜가_null일_경우_예외가_발생한다() {
        // given
        Member member = new Member(1L, "name1", "email1@email.com", "password1", Role.MEMBER);
        // when & then
        Assertions.assertThatThrownBy(
                        () -> Reservation.createWithoutId(
                                member,
                                null,
                                ReservationTime.createWithoutId(LocalTime.of(9, 0)),
                                Theme.createWithoutId("themeName", "des", "th")
                        ))
                .isInstanceOf(ArgumentNullException.class);
    }

    @Test
    void 예약시간이_null일_경우_예외가_발생한다() {
        // given
        Member member = new Member(1L, "name1", "email1@email.com", "password1", Role.MEMBER);
        // when & then
        Assertions.assertThatThrownBy(
                        () -> Reservation.createWithoutId(
                                member,
                                LocalDate.of(2025, 1, 1),
                                null,
                                Theme.createWithoutId("themeName", "des", "th")
                        ))
                .isInstanceOf(ArgumentNullException.class);
    }

    @Test
    void 지나간_시간에_예약을_생성할_경우_예외가_발생한다() {
        // given
        Member member = new Member(1L, "name1", "email1@email.com", "password1", Role.MEMBER);
        // when & then
        Reservation reservation = Reservation.createWithoutId(
                member,
                LocalDate.of(2024, 1, 1),
                new ReservationTime(1L, LocalTime.of(9, 0)),
                Theme.createWithoutId("themeName", "des", "th")
        );
        Assertions.assertThatThrownBy(() -> reservation.validateDateTime())
                .isInstanceOf(PastDateTimeReservationException.class);
    }

    @Test
    void 테마가_null일_경우_예외가_발생한다() {
        // given
        Member member = new Member(1L, "name1", "email1@email.com", "password1", Role.MEMBER);
        // when & then
        Assertions.assertThatThrownBy(
                        () -> Reservation.createWithoutId(
                                member,
                                LocalDate.now().plusDays(1),
                                ReservationTime.createWithoutId(LocalTime.of(9, 0)),
                                null
                        ))
                .isInstanceOf(ArgumentNullException.class);

    }
}