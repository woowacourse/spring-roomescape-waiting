package roomescape.domain.reservation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.domain.exception.BusinessException;
import roomescape.domain.exception.ErrorCode;
import roomescape.domain.theme.Theme;

@DisplayName("예약 슬롯")
class ReservationSlotTest {

    @Test
    @DisplayName("생성하면 식별자 없이 날짜, 시간, 테마를 담는다")
    void create() {
        // given
        ReservationTime time = ReservationTime.of(1L, LocalTime.of(13, 0));
        Theme theme = Theme.of(10L, "도심 탈출", "도심 탈출 설명", "/themes/chase");

        // when
        ReservationSlot slot = ReservationSlot.create(LocalDate.of(2030, 1, 1), time, theme);

        // then
        assertThat(slot.getId()).isNull();
        assertThat(slot.getDate()).isEqualTo(LocalDate.of(2030, 1, 1));
        assertThat(slot.getTime()).isEqualTo(time);
        assertThat(slot.getTheme()).isEqualTo(theme);
    }

    @Test
    @DisplayName("조회 결과를 그대로 담는다")
    void of() {
        // given
        ReservationTime time = ReservationTime.of(1L, LocalTime.of(13, 0));
        Theme theme = Theme.of(10L, "도심 탈출", "도심 탈출 설명", "/themes/chase");

        // when
        ReservationSlot slot = ReservationSlot.of(100L, LocalDate.of(2030, 1, 1), time, theme);

        // then
        assertThat(slot.getId()).isEqualTo(100L);
        assertThat(slot.getDate()).isEqualTo(LocalDate.of(2030, 1, 1));
        assertThat(slot.getTime()).isEqualTo(time);
        assertThat(slot.getTheme()).isEqualTo(theme);
    }

    @Test
    @DisplayName("과거 날짜의 슬롯은 예약할 수 없다")
    void validateIsNotInPastByDate() {
        // given
        ReservationSlot slot = ReservationSlot.of(
                1L,
                LocalDate.of(2030, 1, 1),
                ReservationTime.of(1L, LocalTime.of(13, 0)),
                Theme.of(10L, "도심 탈출", "도심 탈출 설명", "/themes/chase")
        );

        // when & then
        assertThatThrownBy(() -> slot.validateIsNotInPast(LocalDateTime.of(2030, 1, 2, 10, 0)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.RESERVATION_SLOT_IN_PAST);
    }

    @Test
    @DisplayName("같은 날짜라도 지난 시간의 슬롯은 예약할 수 없다")
    void validateIsNotInPastByTime() {
        // given
        ReservationSlot slot = ReservationSlot.of(
                1L,
                LocalDate.of(2030, 1, 1),
                ReservationTime.of(1L, LocalTime.of(9, 0)),
                Theme.of(10L, "도심 탈출", "도심 탈출 설명", "/themes/chase")
        );

        // when & then
        assertThatThrownBy(() -> slot.validateIsNotInPast(LocalDateTime.of(2030, 1, 1, 10, 0)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.RESERVATION_SLOT_IN_PAST);
    }

    @Test
    @DisplayName("현재 시각 이후의 슬롯은 예약할 수 있다")
    void validateIsNotInPastWhenReservable() {
        // given
        ReservationSlot slot = ReservationSlot.of(
                1L,
                LocalDate.of(2030, 1, 1),
                ReservationTime.of(1L, LocalTime.of(11, 0)),
                Theme.of(10L, "도심 탈출", "도심 탈출 설명", "/themes/chase")
        );

        // when & then
        slot.validateIsNotInPast(LocalDateTime.of(2030, 1, 1, 10, 0));
    }
}
