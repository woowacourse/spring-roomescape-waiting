package roomescape.unit.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import roomescape.entity.Reservation;
import roomescape.entity.ReservationTime;
import roomescape.entity.Theme;
import roomescape.global.ReservationStatus;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Sql(scripts = "/sql/reservation-repository-test-data.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
class ReservationRepositoryTest {

    @Autowired
    ReservationRepository reservationRepository;

    @Autowired
    ReservationTimeRepository reservationTimeRepository;

    @Autowired
    ThemeRepository themeRepository;

    @Test
    void 날짜_테마id_상태를_기반으로_모든_예약을_찾는다() {
        //given
        //when
        List<Reservation> actual = reservationRepository.findAllByDateAndThemeIdAndStatus(
                LocalDate.of(2025, 7, 1), 1L, ReservationStatus.RESERVED);

        //then
        assertThat(actual).hasSize(2);
    }

    @Test
    void 날짜_시간_테마_상태를_기반으로_예약을_찾는다() {
        //given
        ReservationTime reservationTime = reservationTimeRepository.findById(1L).get();
        Theme theme = themeRepository.findById(1L).get();

        //when
        Optional<Reservation> actual = reservationRepository.findByDateAndReservationTimeAndThemeAndStatus(
                LocalDate.of(2025, 7, 1), reservationTime, theme, ReservationStatus.RESERVED);

        //then
        assertThat(actual).isPresent();
    }

    @Test
    void 두_날짜_사이에_해당하는_날짜를_가진_예약을_찾는다() {
        //given
        LocalDate start = LocalDate.of(2025, 7, 1);
        LocalDate end = LocalDate.of(2025, 7, 1);

        //when
        List<Reservation> actual = reservationRepository.findAllByDateBetween(start, end);

        //then
        assertThat(actual).hasSize(8);
    }

    private static Stream<Arguments> provideCriteria() {
        return Stream.of(
                Arguments.of(null, null, null, null, 10),
                Arguments.of(10000L, 10000L,
                        LocalDate.of(2025, 1, 1), LocalDate.of(2025, 2, 1), 2),

                Arguments.of(null, 10000L,
                        LocalDate.of(2025, 1, 1), LocalDate.of(2025, 2, 1), 5),

                Arguments.of(10000L, null,
                        LocalDate.of(2025, 1, 1), LocalDate.of(2025, 2, 1), 5),
                Arguments.of(10000L, 10000L, null, LocalDate.of(2025, 1, 2), 2),
                Arguments.of(10000L, 10000L, LocalDate.of(2025, 1, 2), null, 1),
                Arguments.of(null, null,
                        LocalDate.of(2025, 1, 2), LocalDate.of(2025, 1, 3), 4),
                Arguments.of(20000L, 10000L,
                        LocalDate.of(2025, 1, 1), LocalDate.of(2025, 2, 1), 3)
        );
    }

    @ParameterizedTest
    @MethodSource("provideCriteria")
    @Sql(scripts = "/sql/reservation-repository-criteria-test.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
    void null_가능한_기준을_가진_예약을_찾는다(Long memberId, Long themeId, LocalDate from, LocalDate to, int expectedSize) {
        //given
        //when
        List<Reservation> actual = reservationRepository.findAllByFilter(memberId, themeId, from, to);

        //then
        assertThat(actual).hasSize(expectedSize);
    }
}
