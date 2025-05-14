package roomescape.dto;

import java.util.stream.Stream;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import roomescape.dto.request.ReservationTimeRegisterDto;

class ReservationTimeRegisterDtoTest {

    private static Stream<Arguments> testCasesForTimeFormat() {
        return Stream.of(
                Arguments.of("answkduf"),
                Arguments.of("00:61"),
                Arguments.of("25:00")

        );
    }

    @ParameterizedTest
    @MethodSource("testCasesForTimeFormat")
    @DisplayName("시간 형식이 올바르지 않은 경우 예외 처리한다.")
    void test(String input) {
        //given
        ReservationTimeRegisterDto reservationTimeRegisterDto = new ReservationTimeRegisterDto(input);
        // when & then
        assertThatThrownBy(reservationTimeRegisterDto::convertToTime)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("시간");

    }

    @DisplayName("예약 시각은 null 일 수 없다.")
    @Test
    void test2() {
        assertThatThrownBy(() -> new ReservationTimeRegisterDto(null))
                .isInstanceOf(IllegalArgumentException.class);

    }

    @DisplayName("예약 시각은 공백일 수 없다.")
    @Test
    void test3() {
        assertThatThrownBy(() -> new ReservationTimeRegisterDto(" "))
                .isInstanceOf(IllegalArgumentException.class);

    }


}
