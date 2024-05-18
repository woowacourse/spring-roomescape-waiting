package roomescape.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static roomescape.acceptance.PreInsertedData.RESERVATION_CUSTOMER1_THEME3_240502_1100;
import static roomescape.acceptance.PreInsertedData.RESERVATION_CUSTOMER2_THEME3_240502_1200;

class ReservationRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private ReservationRepository reservationRepository;

    static Stream<Arguments> reservationTimeIdProvider() {
        return Stream.of(
                Arguments.of("예약이 있을 때", RESERVATION_CUSTOMER1_THEME3_240502_1100.getReservationTime().getId(), true),
                Arguments.of("예약이 없을 때", 99999L, false)
        );
    }

    @DisplayName("날짜와 테마에 해당하는 예약 목록을 반환한다.")
    @Test
    void findAllByDateAndThemeId() {
        LocalDate date = LocalDate.parse("2024-05-02");
        long themeId = 3L;

        List<Reservation> results = reservationRepository.findAllByDateAndThemeId(date, themeId);

        assertThat(results).containsExactlyInAnyOrder(
                RESERVATION_CUSTOMER1_THEME3_240502_1100,
                RESERVATION_CUSTOMER2_THEME3_240502_1200
        );
    }

    @DisplayName("예약 시간에 해당하는 예약이 있는지 반환한다.")
    @ParameterizedTest(name = "{0}")
    @MethodSource("reservationTimeIdProvider")
    void existsByReservationTimeId(String name, Long reservationTimeId, boolean expected) {
        boolean actual = reservationRepository.existsByReservationTimeId(reservationTimeId);

        assertThat(actual).isEqualTo(expected);
    }

    @DisplayName("주어진 기간동안 가장 많이 예약된 순서대로 예약을 목록 반환한다.")
    @Test
    void findMostReserved() {
        LocalDate from = LocalDate.parse("2024-05-01");
        LocalDate to = LocalDate.parse("2024-05-02");
        List<Reservation> results = reservationRepository.findMostReserved(from, to);

        assertThat(results).containsExactlyInAnyOrder(
                RESERVATION_CUSTOMER1_THEME3_240502_1100,
                RESERVATION_CUSTOMER2_THEME3_240502_1200
        );
    }
}
