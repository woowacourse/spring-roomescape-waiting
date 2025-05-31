package roomescape.dto;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalTime;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import roomescape.dto.request.ReservationTicketRegisterDto;
import roomescape.model.Member;
import roomescape.model.ReservationTime;
import roomescape.model.Role;
import roomescape.model.Theme;

class ReservationTicketRegisterDtoTest {
    private static Stream<Arguments> testCasesForDateFormat() {
        return Stream.of(
                Arguments.of("invalidInput"),
                Arguments.of("2023-10-32"),
                Arguments.of("2023-13-23")
        );
    }

    @ParameterizedTest
    @MethodSource("testCasesForDateFormat")
    @DisplayName("날짜 형식이 올바르지 않은 경우 예외 처리한다.")
    void test(String input) {
        //given
        ReservationTicketRegisterDto reservationTicketRegisterDto = new ReservationTicketRegisterDto(input, 1L, 1L);

        // when & then
        assertThatThrownBy(
                () -> reservationTicketRegisterDto.convertToReservation(new ReservationTime(1L, LocalTime.of(12, 30)),
                        new Theme(1L, "공포", "무서워요", "image"),
                        new Member(1L, "히로", "example@gmail.com", "password", Role.ADMIN)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("날짜");

    }

}
