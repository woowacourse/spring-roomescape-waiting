package roomescape.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static roomescape.PreInsertedData.CUSTOMER_1;
import static roomescape.PreInsertedData.RESERVATION_CUSTOMER1_THEME2_240501_1100;
import static roomescape.PreInsertedData.RESERVATION_CUSTOMER1_THEME2_240501_1200;
import static roomescape.PreInsertedData.RESERVATION_CUSTOMER1_THEME3_240502_1100;
import static roomescape.PreInsertedData.RESERVATION_CUSTOMER2_THEME3_240502_1200;
import static roomescape.PreInsertedData.RESERVATION_CUSTOMER2_THEME3_240503_1200;
import static roomescape.PreInsertedData.RESERVATION_WAITING_CUSTOMER2_THEME2_240501_1100;
import static roomescape.PreInsertedData.RESERVATION_WAITING_CUSTOMER3_THEME2_240501_1100;
import static roomescape.PreInsertedData.THEME_2;
import static roomescape.PreInsertedData.THEME_3;
import static roomescape.domain.Reservation.Status;

class ReservationRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private ReservationRepository reservationRepository;

    @DisplayName("주어진 기간동안 가장 많이 예약된 순서대로 테마 아이디 목록을 반환한다.")
    @Test
    void findMostReserved() {
        LocalDate from = LocalDate.parse("2024-05-01");
        LocalDate to = LocalDate.parse("2024-05-04");
        List<Long> results = reservationRepository.findMostReservedThemesId(from, to);

        assertThat(results).containsExactly(
                THEME_2.getId(),
                THEME_3.getId()
        );
    }

    @DisplayName("조건에 해당하는 예약 목록을 반환한다.")
    @ParameterizedTest
    @MethodSource("filterProvider")
    void filter(Long themeId, Long memberId, LocalDate from, LocalDate to, List<Reservation> expected) {

        List<Reservation> filtered = reservationRepository.filter(themeId, memberId, from, to, Status.RESERVED);

        assertThat(filtered).containsAll(expected);
    }

    static Stream<Arguments> filterProvider() {
        return Stream.of(
                Arguments.of(null, null, null, null,
                        List.of(
                                RESERVATION_CUSTOMER2_THEME3_240503_1200,
                                RESERVATION_CUSTOMER1_THEME3_240502_1100,
                                RESERVATION_CUSTOMER2_THEME3_240502_1200,
                                RESERVATION_CUSTOMER2_THEME3_240503_1200
                        )),
                Arguments.of(THEME_3.getId(), null, null, null,
                        List.of(
                                RESERVATION_CUSTOMER2_THEME3_240503_1200,
                                RESERVATION_CUSTOMER1_THEME3_240502_1100,
                                RESERVATION_CUSTOMER2_THEME3_240502_1200
                        )),
                Arguments.of(null, CUSTOMER_1.getId(), null, null,
                        List.of(
                                RESERVATION_CUSTOMER1_THEME3_240502_1100,
                                RESERVATION_CUSTOMER1_THEME2_240501_1100,
                                RESERVATION_CUSTOMER1_THEME2_240501_1200
                        )),
                Arguments.of(null, null, LocalDate.parse("2024-05-02"), null,
                        List.of(
                                RESERVATION_CUSTOMER1_THEME3_240502_1100,
                                RESERVATION_CUSTOMER2_THEME3_240502_1200,
                                RESERVATION_CUSTOMER2_THEME3_240503_1200
                        )),
                Arguments.of(null, null, null, LocalDate.parse("2024-05-02"),
                        List.of(
                                RESERVATION_CUSTOMER1_THEME2_240501_1100,
                                RESERVATION_CUSTOMER1_THEME3_240502_1100,
                                RESERVATION_CUSTOMER2_THEME3_240502_1200
                        ))
        );
    }

    @DisplayName("같은 날짜, 테마, 시간인 예약 중 주어진 예약 상태와 예약 목록을 구한다.")
    @Test
    void findReservationsWithSameDateThemeTimeAndStatus() {
        Long id = RESERVATION_CUSTOMER1_THEME2_240501_1100.getId();

        Optional<Reservation> waiting = reservationRepository.getFirstReservationWaiting(id);

        assertThat(waiting)
                .isPresent()
                .contains(RESERVATION_WAITING_CUSTOMER2_THEME2_240501_1100);
    }

    @DisplayName("같은 날짜, 테마, 시간인 예약 중 주어진 예약 상태와 동일하면서, 먼저 예약된 수를 구한다.")
    @Test
    void countPreviousReservationsWithSameDateThemeTimeAndStatus() {
        Long id = RESERVATION_WAITING_CUSTOMER3_THEME2_240501_1100.getId();

        Long waitingCountInFrontOfMe = reservationRepository.countPreviousReservationsWithSameDateThemeTimeAndStatus(id, Status.WAITING);

        assertThat(waitingCountInFrontOfMe).isEqualTo(1L);
    }
}
