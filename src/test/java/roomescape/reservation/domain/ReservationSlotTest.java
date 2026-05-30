package roomescape.reservation.domain;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatNoException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import roomescape.global.exception.ConflictException;
import roomescape.global.exception.RoomEscapeException;

class ReservationSlotTest {

    @DisplayName("예약 날짜가 비어있을 때 예외 발생을 테스트합니다.")
    @Test
    void validate_date() {
        assertThatThrownBy(() -> ReservationSlot.builder()
                .date(null)
                .themeId(1L)
                .timeId(1L)
                .startAt(LocalTime.of(9, 0))
                .build())
                .isInstanceOf(RoomEscapeException.class)
                .hasMessage("날짜는 비어있을 수 없습니다.");
    }

    @DisplayName("테마 ID가 올바르지 않을 때 예외 발생을 테스트합니다.")
    @ParameterizedTest
    @NullSource
    @ValueSource(longs = {0L, -1L})
    void validate_theme_id(Long themeId) {
        assertThatThrownBy(() -> ReservationSlot.builder()
                .date(LocalDate.of(2026, 5, 6))
                .themeId(themeId)
                .timeId(1L)
                .startAt(LocalTime.of(9, 0))
                .build())
                .isInstanceOf(RoomEscapeException.class)
                .hasMessage("테마ID는 올바른 값이어야 합니다.");
    }

    @DisplayName("시간 ID가 올바르지 않을 때 예외 발생을 테스트합니다.")
    @ParameterizedTest
    @NullSource
    @ValueSource(longs = {0L, -1L})
    void validate_time_id(Long timeId) {
        assertThatThrownBy(() -> ReservationSlot.builder()
                .date(LocalDate.of(2026, 5, 6))
                .themeId(1L)
                .timeId(timeId)
                .startAt(LocalTime.of(9, 0))
                .build())
                .isInstanceOf(RoomEscapeException.class)
                .hasMessage("시간ID는 올바른 값이어야 합니다.");
    }

    @DisplayName("날짜와 시간을 다른 값으로 변경하면 변경된 슬롯을 반환합니다.")
    @Test
    void update_date_and_time() {
        ReservationSlot reservationSlot = ReservationSlot.builder()
                .date(LocalDate.of(2026, 5, 6))
                .themeId(1L)
                .timeId(1L)
                .startAt(LocalTime.of(10, 0))
                .build();

        ReservationSlot updated = reservationSlot.updateDateAndTime(LocalDate.of(2028, 6, 1), 2L,
                LocalTime.of(11, 0));

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(updated.date()).isEqualTo(LocalDate.of(2028, 6, 1));
            softly.assertThat(updated.themeId()).isEqualTo(1L);
            softly.assertThat(updated.timeId()).isEqualTo(2L);
            softly.assertThat(updated.startAt()).isEqualTo(LocalTime.of(11, 0));
        });
    }

    @DisplayName("동일한 날짜와 시간으로 변경 시 예외 발생을 테스트합니다.")
    @Test
    void update_date_and_time_same_value_exception() {
        ReservationSlot reservationSlot = ReservationSlot.builder()
                .date(LocalDate.of(2026, 5, 6))
                .themeId(1L)
                .timeId(1L)
                .startAt(LocalTime.of(10, 0))
                .build();

        assertThatThrownBy(() -> reservationSlot.updateDateAndTime(LocalDate.of(2026, 5, 6), 1L,
                LocalTime.of(10, 0)))
                .isInstanceOf(ConflictException.class)
                .hasMessage("동일한 날짜와 시간으로 변경할 수 없습니다.");
    }

    @DisplayName("현재 시간 이후의 슬롯이면 예약이 가능합니다.")
    @Test
    void validate_reservable_future() {
        LocalDateTime now = LocalDateTime.of(2026, 5, 17, 10, 0);
        ReservationSlot reservationSlot = ReservationSlot.builder()
                .date(LocalDate.of(2026, 5, 17))
                .themeId(1L)
                .timeId(1L)
                .startAt(LocalTime.of(11, 0))
                .build();

        assertThatNoException().isThrownBy(() -> reservationSlot.validateReservable(now));
    }

    @DisplayName("현재 시간보다 이전 시간으로 예약 시 예외 발생을 테스트합니다.")
    @Test
    void validate_reservable_past_exception() {
        LocalDateTime now = LocalDateTime.of(2026, 5, 17, 10, 0);
        ReservationSlot reservationSlot = ReservationSlot.builder()
                .date(LocalDate.of(2026, 5, 17))
                .themeId(1L)
                .timeId(1L)
                .startAt(LocalTime.of(9, 0))
                .build();

        assertThatThrownBy(() -> reservationSlot.validateReservable(now))
                .isInstanceOf(RoomEscapeException.class)
                .hasMessage("현재 시간보다 이전 시간으로 예약을 할 수 없습니다.");
    }

    @DisplayName("현재 시간 이후의 슬롯이면 삭제가 가능합니다.")
    @Test
    void validate_deletable_future() {
        LocalDateTime now = LocalDateTime.of(2026, 5, 17, 10, 0);
        ReservationSlot reservationSlot = ReservationSlot.builder()
                .date(LocalDate.of(2026, 5, 17))
                .themeId(1L)
                .timeId(1L)
                .startAt(LocalTime.of(11, 0))
                .build();

        assertThatNoException().isThrownBy(() -> reservationSlot.validateDeletable(now));
    }

    @DisplayName("이미 지나간 슬롯 삭제 시 예외 발생을 테스트합니다.")
    @Test
    void validate_deletable_past_exception() {
        LocalDateTime now = LocalDateTime.of(2026, 5, 17, 10, 0);
        ReservationSlot reservationSlot = ReservationSlot.builder()
                .date(LocalDate.of(2026, 5, 17))
                .themeId(1L)
                .timeId(1L)
                .startAt(LocalTime.of(9, 0))
                .build();

        assertThatThrownBy(() -> reservationSlot.validateDeletable(now))
                .isInstanceOf(RoomEscapeException.class)
                .hasMessage("이미 지나간 예약은 삭제할 수 없습니다.");
    }

    @DisplayName("현재 시간 이후의 슬롯이면 변경이 가능합니다.")
    @Test
    void validate_updatable_future() {
        LocalDateTime now = LocalDateTime.of(2026, 5, 17, 10, 0);
        ReservationSlot reservationSlot = ReservationSlot.builder()
                .date(LocalDate.of(2026, 5, 17))
                .themeId(1L)
                .timeId(1L)
                .startAt(LocalTime.of(11, 0))
                .build();

        assertThatNoException().isThrownBy(() -> reservationSlot.validateUpdatable(now));
    }

    @DisplayName("이미 지나간 슬롯 변경 시 예외 발생을 테스트합니다.")
    @Test
    void validate_updatable_past_exception() {
        LocalDateTime now = LocalDateTime.of(2026, 5, 17, 10, 0);
        ReservationSlot reservationSlot = ReservationSlot.builder()
                .date(LocalDate.of(2026, 5, 17))
                .themeId(1L)
                .timeId(1L)
                .startAt(LocalTime.of(9, 0))
                .build();

        assertThatThrownBy(() -> reservationSlot.validateUpdatable(now))
                .isInstanceOf(RoomEscapeException.class)
                .hasMessage("이미 지나간 예약은 변경할 수 없습니다.");
    }
}
