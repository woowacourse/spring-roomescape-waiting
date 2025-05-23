package roomescape.reservation.application.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static roomescape.reservation.domain.ReservationFixtures.persistReservedReservation;
import static roomescape.reservation.domain.ReservationFixtures.persistWaitingReservation;
import static roomescape.reservation.time.domain.ReservationTimeFixtures.persistReservationTime;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberFixtures;
import roomescape.reservation.application.ReservationRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationFixtures;
import roomescape.reservation.domain.ReservationWithRank;
import roomescape.reservation.time.domain.ReservationTime;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.ThemeFixtures;

@ActiveProfiles("test")
@DataJpaTest
class ReservationRepositoryTest {

    @Autowired
    private ReservationRepository reservationRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    @DisplayName("회원ID, 테마ID, 날짜 범위로 예약 목록을 조회한다")
    void findAllByMemberIdAndThemeIdAndDateBetweenTest() {
        // given
        TestData testData = setUpTestData();
        flushAndClear();

        // when
        List<Reservation> nullResult = reservationRepository.findAllByMemberIdAndThemeIdAndDateBetween(
                null, null, null, null);
        List<Reservation> memberIdResult = reservationRepository.findAllByMemberIdAndThemeIdAndDateBetween(
                testData.member1.getId(), null, null, null);
        List<Reservation> themeResult = reservationRepository.findAllByMemberIdAndThemeIdAndDateBetween(
                null, testData.theme1.getId(), null, null);
        List<Reservation> dateResult = reservationRepository.findAllByMemberIdAndThemeIdAndDateBetween(
                null, null, LocalDate.now().minusDays(3), LocalDate.now());

        // then
        assertAll(
                () -> assertThat(nullResult).hasSize(4),
                () -> assertThat(memberIdResult).hasSize(2),
                () -> assertThat(themeResult).hasSize(2),
                () -> assertThat(dateResult).hasSize(4)
        );
    }

    @Test
    @DisplayName("날짜, 시간, 테마가 같은 최근 WAITING 상태 예약 정보를 반환한다")
    void findFirstWaitingReservationTest() {
        // given
        LocalDate date = LocalDate.of(2025, 5, 5);
        ReservationTime reservationTime = persistReservationTime(entityManager);
        Theme theme = ThemeFixtures.persistTheme(entityManager);

        persistReservedReservation(entityManager, theme, date, reservationTime);
        Reservation waitingReservation = persistWaitingReservation(entityManager, theme, date, reservationTime);
        persistWaitingReservation(entityManager, theme, date, reservationTime);

        flushAndClear();

        // when
        Reservation result = reservationRepository.findFirstWaitingReservation(date, reservationTime, theme);

        // then
        assertThat(result.getId()).isEqualTo(waitingReservation.getId());
    }

    @Test
    @DisplayName("특정 날짜와 테마 ID로 예약을 찾을 수 있다")
    void findAllByDateAndThemeId() {
        // given
        Theme theme = ThemeFixtures.persistTheme(entityManager);
        LocalDate date = LocalDate.now();

        ReservationFixtures.persistReservedReservation(entityManager, theme, date);
        flushAndClear();

        // when
        List<Reservation> results = reservationRepository.findAllByDateAndThemeId(date, theme.getId());

        // then
        assertAll(
                () -> assertThat(results).hasSize(1),
                () -> assertThat(results.get(0).getDate()).isEqualTo(date),
                () -> assertThat(results.get(0).getTheme().getId()).isEqualTo(theme.getId())
        );
    }

    @Test
    @DisplayName("회원의 모든 예약 정보와 대기 순위를 조회할 수 있다")
    void findReservationsWithRankByMemberId() {
        // given
        Member member = MemberFixtures.persistUserMember(entityManager);
        Theme theme = ThemeFixtures.persistTheme(entityManager);
        ReservationTime reservationTime = persistReservationTime(entityManager);

        persistReservedReservation(entityManager, member, theme, reservationTime);
        persistWaitingReservation(entityManager, member, theme, reservationTime);
        persistWaitingReservation(entityManager, member, theme, reservationTime);
        flushAndClear();

        // when
        List<ReservationWithRank> results = reservationRepository
                .findReservationsWithRankByMemberId(member.getId());

        // then
        assertAll(
                () -> assertThat(results).hasSize(3),
                () -> assertThat(results.get(1).rank()).isEqualTo(1L),
                () -> assertThat(results.get(2).rank()).isEqualTo(2L)
        );
    }

    @Test
    @DisplayName("대기 상태인 모든 예약 정보와 대기 순위를 조회할 수 있다")
    void findReservationsWithRankOfWaitingStatus() {
        // given
        Member member = MemberFixtures.persistUserMember(entityManager);
        Theme theme = ThemeFixtures.persistTheme(entityManager);
        ReservationTime reservationTime = persistReservationTime(entityManager);

        persistReservedReservation(entityManager, member, theme, reservationTime);
        persistWaitingReservation(entityManager, member, theme, reservationTime);
        persistWaitingReservation(entityManager, member, theme, reservationTime);

        // when
        List<ReservationWithRank> results = reservationRepository.findReservationsWithRankOfWaitingStatus();

        // then
        assertAll(
                () -> assertThat(results).hasSize(2),
                () -> assertThat(results).extracting("rank")
                        .containsExactly(1L, 2L)
        );
    }

    @Test
    @DisplayName("특정 예약 시간 ID에 대한 예약 존재 여부를 확인할 수 있다")
    void existsByReservationTimeId() {
        // given
        ReservationTime reservationTime = persistReservationTime(entityManager);

        persistReservedReservation(entityManager, reservationTime);

        // when & then
        assertAll(
                () -> assertThat(reservationRepository.existsByReservationTimeId(reservationTime.getId())).isTrue(),
                () -> assertThat(reservationRepository.existsByReservationTimeId(999L)).isFalse()
        );
    }

    @Test
    @DisplayName("특정 날짜와 시작 시간에 대한 예약 존재 여부를 확인할 수 있다")
    void existsByDateAndReservationTimeStartAt() {
        // given
        LocalDate date = LocalDate.now();
        LocalTime startAt = LocalTime.of(10, 0);
        ReservationTime reservationTime = persistReservationTime(entityManager, startAt);

        persistReservedReservation(entityManager, date, reservationTime);

        // when & then
        assertAll(
                () -> assertThat(reservationRepository.existsByDateAndReservationTimeStartAt(
                        date, startAt)).isTrue(),
                () -> assertThat(reservationRepository.existsByDateAndReservationTimeStartAt(
                        date.plusDays(1), startAt)).isFalse(),
                () -> assertThat(reservationRepository.existsByDateAndReservationTimeStartAt(
                        date, LocalTime.of(11, 0))).isFalse()
        );
    }

    @Test
    @DisplayName("특정 테마 ID에 대한 예약 존재 여부를 확인할 수 있다")
    void existsByThemeId() {
        // given
        Theme theme = ThemeFixtures.persistTheme(entityManager);
        ReservationFixtures.persistReservedReservation(entityManager, theme);

        // when && then
        assertAll(
                () -> assertThat(reservationRepository.existsByThemeId(theme.getId())).isTrue(),
                () -> assertThat(reservationRepository.existsByThemeId(999L)).isFalse()
        );
    }

    private TestData setUpTestData() {
        Member member1 = MemberFixtures.persistUserMember(entityManager);
        Member member2 = MemberFixtures.persistUserMember(entityManager);
        Theme theme1 = ThemeFixtures.persistTheme(entityManager);
        Theme theme2 = ThemeFixtures.persistTheme(entityManager);

        persistReservedReservation(entityManager, member1, theme1, LocalDate.now());
        persistReservedReservation(entityManager, member1, theme2, LocalDate.now().minusDays(1));
        persistReservedReservation(entityManager, member2, theme1, LocalDate.now().minusDays(2));
        persistReservedReservation(entityManager, member2, theme2, LocalDate.now().minusDays(3));
        return new TestData(member1, member2, theme1, theme2);
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }

    private record TestData(
            Member member1,
            Member member2,
            Theme theme1,
            Theme theme2
    ) {
    }
}
