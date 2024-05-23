package roomescape.reservation.model;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import roomescape.fixture.MemberFixture;
import roomescape.fixture.ReservationTimeFixture;
import roomescape.fixture.ThemeFixture;
import roomescape.fixture.WaitingFixture;

class WaitingTest {

    @Nested
    class createWaiting {

        @Test
        @DisplayName("예약 대기 객체 생성 시 예약 대기자가 없는 경우 예외를 반환한다.")
        void createWaiting_WhenNameIsBlank() {
            assertThatThrownBy(
                    () -> WaitingFixture.getOneWithMember(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("예약 대기 생성 시 예약 대기자는 필수입니다.");
        }

        @Test
        @DisplayName("예약 대기 객체 생성 시 예약 대기자 명이 공백인 경우 예외를 반환한다.")
        void createWaiting_WhenNameOverLength() {
            assertThatThrownBy(
                    () -> WaitingFixture.getOneWithMember(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("예약 대기 생성 시 예약 대기자는 필수입니다.");
        }

        @Test
        @DisplayName("예약 대기 객체 생성 시 예약 대기 날짜가 공백인 경우 예외를 반환한다.")
        void createWaiting_WhenWaitingDateIsNull() {
            assertThatThrownBy(
                    () -> WaitingFixture.getOneWithDateTimeTheme(null, ReservationTimeFixture.getOne(),
                            ThemeFixture.getOne()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("예약 대기 생성 시 예약 대기 날짜는 필수입니다.");
        }

        @Test
        @DisplayName("예약 대기 객체 생성 시 예약 대기 시간이 공백인 경우 예외를 반환한다.")
        void createWaiting_WhenReservationTimeIsNull() {
            assertThatThrownBy(
                    () -> WaitingFixture.getOneWithTimeTheme(null, ThemeFixture.getOne()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("예약 대기 생성 시 예약 대기 시간은 필수입니다.");
        }

        @Test
        @DisplayName("예약 대기 객체 생성 시 예약 대기 테마가 공백인 경우 예외를 반환한다.")
        void createWaiting_WhenWaitingThemeIsNull() {
            assertThatThrownBy(
                    () -> WaitingFixture.getOneWithTheme(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("예약 대기 생성 시 예약 대기 테마는 필수입니다.");
        }

        @Test
        @DisplayName("예약 대기 객체 생성 시 예약 대기하려는 날짜가 과거인 경우 예외를 반환한다.")
        void createWaiting_WhenReservationTimeInPast() {
            LocalDateTime dateTime = LocalDateTime.now().minusHours(1);
            assertThatThrownBy(
                    () -> Waiting.create(
                            MemberFixture.getOne(),
                            dateTime.toLocalDate(),
                            new ReservationTime(dateTime.toLocalTime()),
                            ThemeFixture.getOne()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage(dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm"))
                            + "는 지나간 시간임으로 예약 대기 생성이 불가능합니다. 현재 이후 날짜로 재예약해주세요.");
        }
    }
}
