package roomescape.reservation.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static roomescape.reservation.repository.fixture.MemberFixture.MEMBER1;
import static roomescape.reservation.repository.fixture.ReservationFixture.RESERVATION1;
import static roomescape.reservation.repository.fixture.ReservationFixture.RESERVATION2;
import static roomescape.reservation.repository.fixture.ReservationFixture.RESERVATION4;
import static roomescape.reservation.repository.fixture.ReservationFixture.RESERVATION5;
import static roomescape.reservation.repository.fixture.ReservationFixture.RESERVATION6;
import static roomescape.reservation.repository.fixture.ReservationTimeFixture.TIME1;
import static roomescape.reservation.repository.fixture.ThemeFixture.THEME1;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;
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
import roomescape.reservation.repository.fixture.ReservationFixture;

@DataJpaTest
class ReservationRepositoryTest {

    @Autowired
    private ReservationRepository reservationRepository;

    @DisplayName("모든 예약을 조회한다.")
    @Test
    void findAll() {
        final var result = reservationRepository.findAll();

        assertThat(result).hasSize(ReservationFixture.count());
    }

    @DisplayName("id로 예약을 조회한다.")
    @Test
    void findById() {
        final var result = reservationRepository.findById(1L);

        assertThat(result.get()).isEqualTo(RESERVATION1.create());
    }

    @DisplayName("예약을 생성한다.")
    @Test
    void save() {
        final var reservation = Reservation.of(
                MEMBER1.create(),
                LocalDate.parse("2024-07-01"),
                TIME1.create(),
                THEME1.create(),
                Status.RESERVATION
        );

        reservationRepository.save(reservation);

        assertThat(reservationRepository.findAll()).hasSize(ReservationFixture.count() + 1);
    }

    @DisplayName("예약을 삭제한다.")
    @Test
    void delete() {
        reservationRepository.deleteById(1L);

        assertThat(reservationRepository.findById(1L)).isEmpty();
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
        LocalDate startDate = RESERVATION1.create().getDate();
        LocalDate endDate = RESERVATION2.create().getDate();
        final var result = reservationRepository.findByDateBetween(startDate, endDate);

        assertThat(result).extracting(Reservation::getDate)
                .isNotEmpty()
                .allMatch(date -> (date.isEqual(startDate) || date.isAfter(startDate))
                        && (date.isEqual(endDate) || date.isBefore(endDate)));
    }

    @DisplayName("날짜, 예약 시간 id, 테마 id로 예약을 조회한다.")
    @Test
    void findByDateAndTimeIdAndThemeId() {
        Reservation reservation = RESERVATION4.create();
        final var results = reservationRepository.findByDateAndTimeIdAndThemeId(
                reservation.getDate(), reservation.getTime().getId(), reservation.getTheme().getId()
        );

        assertThat(results)
                .allMatch(result -> result.getDate().equals(reservation.getDate())
                        && result.getTime().getId() == reservation.getTime().getId()
                        && result.getTheme().getId() == reservation.getTheme().getId()
                );
    }

    @DisplayName("날짜, 시간 id, 테마 id, 생성 날짜 이전 예약 개수를 조회한다.")
    @ParameterizedTest
    @MethodSource("getDateAndTimeIdAndThemeIdAndCreatedAt")
    void countByDateAndTimeIdAndThemeIdAndCreatedAtBefore(Reservation reservation, int count) {
        int result = reservationRepository.countByDateAndTimeIdAndThemeIdAndCreatedAtBefore(
                reservation.getDate(),
                reservation.getTime().getId(),
                reservation.getTheme().getId(),
                reservation.getCreatedAt()
        );

        assertThat(result).isEqualTo(count);
    }

    private static Stream<Arguments> getDateAndTimeIdAndThemeIdAndCreatedAt() {
        return Stream.of(
                Arguments.of(RESERVATION5.create(), 1),
                Arguments.of(RESERVATION6.create(), 2)
        );
    }

    @DisplayName("날짜, 테마 id를 기준으로 예약의 시간 id를 조회한다.")
    @Test
    void findByDateAndThemeId() {
        LocalDate date = LocalDate.parse("2024-06-30");
        long themeId = 2L;

        final var results = reservationRepository.findByDateAndThemeId(date, themeId);

        assertThat(results)
                .allMatch(result -> result.getDate().equals(date) && result.getTheme().getId() == themeId);
    }

    @DisplayName("테마 id, 멤버 id, 상태, 날짜 사이로 예약을 조회한다.")
    @ParameterizedTest
    @MethodSource("getThemeIdAndMemberIdAndStatusDateBetween")
    void findByThemeIdAndMemberIdAndStatusAndDateBetween(Long themeId,
                                                         Long memberId,
                                                         Optional<Status> status,
                                                         LocalDate dateFrom,
                                                         LocalDate dateTo) {
        final var result = reservationRepository.findByThemeIdAndMemberIdAndStatusAndDateBetween(
                themeId, memberId, status, dateFrom, dateTo
        );

        assertThat(result).allMatch(matchCondition(themeId, memberId, dateFrom, dateTo));
    }

    private static Stream<Arguments> getThemeIdAndMemberIdAndStatusDateBetween() {
        LocalDate dateFrom = LocalDate.parse("2024-12-13");
        LocalDate dateTo = LocalDate.parse("2024-12-24");

        return Stream.of(
                Arguments.of(1L, null, Optional.empty(), null, null),
                Arguments.of(null, 1L, Optional.empty(), null, null),
                Arguments.of(null, null, Optional.empty(), dateFrom, null),
                Arguments.of(null, null, Optional.empty(), null, dateTo),
                Arguments.of(null, null, Optional.empty(), dateFrom, dateTo),
                Arguments.of(3L, 2L, Optional.empty(), dateFrom, dateTo),
                Arguments.of(null, null, Optional.of(Status.WAITING), null, null)
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

    @DisplayName("날짜, 시간 id, 테마 id로 예약을 조회하고 created_at을 기준으로 정렬하여 첫 번째 예약을 조회한다.")
    @Test
    void findFirstByDateAndTimeIdAndThemeIdOrderByCreatedAt() {
        Reservation result = reservationRepository.findFirstByDateAndTimeIdAndThemeIdOrderByCreatedAt(
                LocalDate.parse("2024-06-30"), 1L, 2L
        ).get();

        assertThat(result).isEqualTo(RESERVATION4.create());
    }
}
