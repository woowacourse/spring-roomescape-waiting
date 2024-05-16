package roomescape.domain.reservation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.context.jdbc.SqlGroup;
import roomescape.domain.schedule.ReservationDate;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ReservationRepositoryTest {
    @Autowired
    private ReservationRepository reservationRepository;

    static Stream<Arguments> findByConditionArguments() {
        LocalDate today = LocalDate.now();
        LocalDate threeDaysBefore = today.minusDays(3);
        LocalDate sevenDaysBefore = today.minusDays(7);
        return Stream.of(
                Arguments.of(
                        1L, null, null, null,
                        List.of(1, 4, 7, 10, 13)),
                Arguments.of(null, 2L, null, null,
                        List.of(2, 7, 12)),
                Arguments.of(null, null, ReservationDate.of(threeDaysBefore), null,
                        List.of(1, 2, 5, 6, 9, 10, 13, 14)),
                Arguments.of(null, null, null, ReservationDate.of(threeDaysBefore),
                        List.of(3, 4, 7, 8, 11, 12, 15)),
                Arguments.of(null, null, ReservationDate.of(sevenDaysBefore), ReservationDate.of(threeDaysBefore),
                        List.of(3, 4, 7, 8, 11, 12, 15)),
                Arguments.of(2L, 3L, null, null,
                        List.of(8))
        );
    }

    @DisplayName("주어진 조건으로 필터링하여 검색한다.")
    @ParameterizedTest
    @MethodSource("findByConditionArguments")
    @SqlGroup({
            @Sql(value = "/truncate.sql", executionPhase = ExecutionPhase.AFTER_TEST_METHOD),
            @Sql("/insert-reservations-for-filtering.sql")
    })
    void findByTest(Long memberId, Long themeId, ReservationDate dateFrom, ReservationDate dateTo,
                    List<Integer> expected) {
        //when
        List<Reservation> reservations = reservationRepository.findBy(memberId, themeId, dateFrom, dateTo);
        List<Integer> result = reservations.stream()
                .map(Reservation::getId)
                .map(Long::intValue)
                .toList();

        //then
        assertThat(result).isEqualTo(expected);

    }
}
