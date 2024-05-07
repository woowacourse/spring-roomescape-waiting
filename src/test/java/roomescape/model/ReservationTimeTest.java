package roomescape.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import roomescape.service.dto.ReservationTimeDto;

import java.time.LocalTime;

import static org.assertj.core.api.Assertions.*;

public class ReservationTimeTest {

    @DisplayName("데이터의 id가 0 이하인 경우 예외를 발생시킨다.")
    @ParameterizedTest
    @ValueSource(longs = {0, -1, -999})
    void should_throw_exception_when_invalid_id(long id) {
        assertThatThrownBy(() -> new ReservationTime(id, LocalTime.now()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("id는 0 이하일 수 없습니다.");
    }

    @DisplayName("데이터의 시간이 null인 경우 예외를 발생시킨다.")
    @Test
    void should_throw_exception_when_invalid_time() {
        assertThatThrownBy(() -> new ReservationTime(1L, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("데이터는 null일 수 없습니다.");
    }

    @DisplayName("데이터가 유효한 경우 예외가 발생하지 않는다.")
    @ParameterizedTest
    @CsvSource(value = {"1,01:00", "2,23:59"})
    void should_not_throw_exception_when_valid_data(long id, String rawStartAt) {
        LocalTime startAt = LocalTime.parse(rawStartAt);
        assertThatCode(() -> new ReservationTime(id, startAt))
                .doesNotThrowAnyException();
    }

    @DisplayName("DTO를 도메인으로 변환하는 경우 id는 0이 될 수 있다.")
    @Test
    void should_be_zero_id_when_convert_dto() {
        ReservationTimeDto reservationTimeDto = new ReservationTimeDto(LocalTime.now());
        assertThatCode(() -> {
            ReservationTime time = ReservationTime.from(reservationTimeDto);
            assertThat(time.getId()).isEqualTo(0);
        }).doesNotThrowAnyException();
    }
}
