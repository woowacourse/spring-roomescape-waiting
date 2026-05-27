package roomescape.reservation.domain;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import roomescape.global.exception.RoomEscapeException;

class WaitingTest {

    @DisplayName("예약 대기자 이름이 비어있을 때 예외 발생을 테스트합니다.")
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    void validate_name(String name) {
        assertThatThrownBy(() -> {
            ReservationSlot slot = ReservationSlot.builder()
                    .date(LocalDate.of(2026, 5, 6))
                    .themeId(1L)
                    .timeId(1L)
                    .startAt(LocalTime.of(9, 0))
                    .build();

            Waiting.builder()
                    .name(name)
                    .slot(slot)
                    .build();
        })
                .isInstanceOf(RoomEscapeException.class)
                .hasMessage("이름은 비어있을 수 없습니다.");
    }
}
