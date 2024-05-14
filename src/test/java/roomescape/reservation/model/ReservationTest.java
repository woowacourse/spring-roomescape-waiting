package roomescape.reservation.model;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import roomescape.fixture.MemberFixture;
import roomescape.fixture.ReservationFixture;
import roomescape.fixture.ReservationTimeFixture;
import roomescape.fixture.ThemeFixture;
import roomescape.reservationtime.model.ReservationTime;

class ReservationTest {

    @Nested
    class createReservation {

        @Test
        @DisplayName("예약 객체 생성 시 예약자가 없는 경우 예외를 반환한다.")
        void createReservation_WhenNameIsBlank() {
            assertThatThrownBy(
                    () -> ReservationFixture.getOneWithMember(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("예약 생성 시 예약자는 필수입니다.");
        }

        @Test
        @DisplayName("예약 객체 생성 시 예약자 명이 공백인 경우 예외를 반환한다.")
        void createReservation_WhenNameOverLength() {
            assertThatThrownBy(
                    () -> ReservationFixture.getOneWithMember(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("예약 생성 시 예약자는 필수입니다.");
        }

        @Test
        @DisplayName("예약 객체 생성 시 예약 날짜가 공백인 경우 예외를 반환한다.")
        void createReservation_WhenReservationDateIsNull() {
            assertThatThrownBy(
                    () -> ReservationFixture.getOneWithDateTimeTheme(null, ReservationTimeFixture.getOne(),
                            ThemeFixture.getOne()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("예약 생성 시 예약 날짜는 필수입니다.");
        }

        @Test
        @DisplayName("예약 객체 생성 시 예약 시간이 공백인 경우 예외를 반환한다.")
        void createReservation_WhenReservationTimeIsNull() {
            assertThatThrownBy(
                    () -> ReservationFixture.getOneWithTimeTheme(null, ThemeFixture.getOne()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("예약 생성 시 예약 시간은 필수입니다.");
        }

        @Test
        @DisplayName("예약 객체 생성 시 예약 테마가 공백인 경우 예외를 반환한다.")
        void createReservation_WhenReservationThemeIsNull() {
            assertThatThrownBy(
                    () -> ReservationFixture.getOneWithTheme(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("예약 생성 시 예약 테마는 필수입니다.");
        }

        @Test
        @DisplayName("예약 객체 생성 시 예약하려는 날짜가 과거인 경우 예외를 반환한다.")
        void createReservation_WhenReservationDateInPast() {
            assertThatThrownBy(
                    () -> Reservation.create(
                            MemberFixture.getOne(),
                            LocalDate.parse("2024-01-01"),
                            ReservationTimeFixture.getOne(),
                            ThemeFixture.getOne()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("2024-01-01는 지나간 시간임으로 예약 생성이 불가능합니다. 현재 이후 날짜로 재예약해주세요.");
        }

        @Test
        @DisplayName("예약 객체 생성 시 예약하려는 날짜가 과거인 경우 예외를 반환한다.")
        void createReservation_WhenReservationTimeInPast() {
            LocalDateTime now = LocalDateTime.now();
            assertThatThrownBy(
                    () -> Reservation.create(
                            MemberFixture.getOne(),
                            now.toLocalDate(),
                            new ReservationTime(null, now.toLocalTime().minusHours(1)),
                            ThemeFixture.getOne()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage(now.minusHours(1) + "는 현재보다 동일하거나 지나간 시간임으로 예약 생성이 불가능합니다. 현재 이후 날짜로 재예약해주세요.");
        }
    }
}
