package roomescape.reservation.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDate;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.Status;

@DataJpaTest
class ReservationRepositoryTest {

    private static final int DEFAULT_RESERVATION_COUNT = 3;

    @Autowired
    private ReservationRepository reservationRepository;

    @DisplayName("모든 예약을 조회한다.")
    @Test
    void findAll() {
        final var result = reservationRepository.findAll();

        assertThat(result).hasSize(DEFAULT_RESERVATION_COUNT);
    }

    @DisplayName("id로 예약을 조회한다.")
    @Test
    void findById() {
        final var result = reservationRepository.findById(1L);

        assertThat(result.get().getId()).isEqualTo(1L);
    }

    @DisplayName("예약을 생성한다.")
    @Test
    void save() {
        final var reservation = Reservation.of(
                Fixture.MEMBER1, Fixture.DATE, Fixture.RESERVATION_TIME1, Fixture.THEME1, Status.RESERVATION
        );

        reservationRepository.save(reservation);

        assertThat(reservationRepository.findAll()).hasSize(DEFAULT_RESERVATION_COUNT + 1);
    }

    @DisplayName("id로 예약을 삭제한다.")
    @Test
    void deleteById() {
        final var result = reservationRepository.deleteById(3);

        assertAll(
                () -> assertThat(result).isEqualTo(1),
                () -> assertThat(reservationRepository.findAll())
                        .extracting(Reservation::getId)
                        .doesNotContain(3L)
        );
    }

    @DisplayName("예약 시간 id로 예약을 조회한다.")
    @Test
    void findByTimeId() {
        final var result = reservationRepository.findByTimeId(1L);

        assertThat(result).extracting(Reservation::getTime)
                .isNotEmpty()
                .allMatch(time -> time.getId() == 1L);
    }

    @DisplayName("테마 id로 예약을 조회한다.")
    @Test
    void findByThemeId() {
        final var result = reservationRepository.findByThemeId(1L);

        assertThat(result).extracting(Reservation::getTheme)
                .isNotEmpty()
                .allMatch(theme -> theme.getId() == 1L);
    }

    @DisplayName("멤버 id로 예약을 조회한다.")
    @Test
    void findByMemberId() {
        final var result = reservationRepository.findByMemberId(1L);

        assertThat(result).extracting(Reservation::getMember)
                .isNotEmpty()
                .allMatch(member -> member.getId() == 1L);
    }

    @DisplayName("날짜 사이로 예약을 조회한다.")
    @Test
    void findByDateBetween() {
        LocalDate startDate = LocalDate.parse("2024-12-23");
        LocalDate endDate = LocalDate.parse("2024-12-25");
        final var result = reservationRepository.findByDateBetween(startDate, endDate);

        assertThat(result).extracting(Reservation::getDate)
                .isNotEmpty()
                .allMatch(date -> (date.isEqual(startDate) || date.isAfter(startDate))
                        && (date.isEqual(endDate) || date.isBefore(endDate)));
    }

    @DisplayName("날짜, 예약 시간 id, 테마 id로 예약을 조회한다.")
    @Test
    void findByDateAndTimeIdAndThemeId() {
        final var result = reservationRepository.findByDateAndTimeIdAndThemeId(
                LocalDate.parse("2024-12-12"), 1L, 1L
        );

        assertThat(result).extracting(Reservation::getId).containsExactly(1L);
    }

    @DisplayName("날짜, 테마 id를 기준으로 예약의 시간 id를 조회한다.")
    @Test
    void findByDateAndThemeId() {
        final var result = reservationRepository.findByDateAndThemeId(
                LocalDate.parse("2024-12-12"), 1L
        );

        assertThat(result)
                .hasSize(1)
                .extracting(reservation -> reservation.getTime().getId())
                .containsExactly(1L);
    }

    @DisplayName("테마 id, 멤버 id, 날짜 사이로 얘약을 조회한다.")
    @ParameterizedTest
    @MethodSource("getThemeIdAndMemberIdAndDateBetween")
    void findByThemeIdAndMemberIdAndDateBetween(Long themeId,
                                                Long memberId,
                                                LocalDate dateFrom,
                                                LocalDate dateTo,
                                                int size) {
        final var result = reservationRepository.findByThemeIdAndMemberIdAndDateBetween(
                themeId, memberId, dateFrom, dateTo
        );

        assertThat(result)
                .hasSize(size)
                .allMatch(matchCondition(themeId, memberId, dateFrom, dateTo));
    }

    private static Stream<Arguments> getThemeIdAndMemberIdAndDateBetween() {
        LocalDate dateFrom = LocalDate.parse("2024-12-13");
        LocalDate dateTo = LocalDate.parse("2024-12-24");

        return Stream.of(
                Arguments.of(null, null, null, null, DEFAULT_RESERVATION_COUNT),
                Arguments.of(1L, null, null, null, 1),
                Arguments.of(null, 1L, null, null, 2),
                Arguments.of(null, null, dateFrom, null, 2),
                Arguments.of(null, null, null, dateTo, 2),
                Arguments.of(null, null, dateFrom, dateTo, 1),
                Arguments.of(3L, 2L, dateFrom, dateTo, 0)
        );
    }

    private static Predicate<Reservation> matchCondition(Long themeId, Long memberId, LocalDate dateFrom,
                                                         LocalDate dateTo) {
        return reservation -> (themeId == null || Objects.equals(reservation.getTheme().getId(), themeId))
                && (memberId == null || Objects.equals(reservation.getMember().getId(), memberId))
                && (dateFrom == null || reservation.getDate().isEqual(dateFrom) || reservation.getDate()
                .isAfter(dateFrom))
                && (dateTo == null || reservation.getDate().isEqual(dateTo) || reservation.getDate().isBefore(dateTo));
    }
}
