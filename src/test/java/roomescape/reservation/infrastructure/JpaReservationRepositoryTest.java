package roomescape.reservation.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.domain.ReservationWithRank;
import roomescape.reservation.infrastructure.jpa.JpaReservationRepository;
import roomescape.reservationTime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

@Sql(scripts = "/data/reservationConditionTest.sql")
@DataJpaTest
class JpaReservationRepositoryTest {

    @Autowired
    private JpaReservationRepository repository;

    @Autowired
    private EntityManager em;

    @Test
    @DisplayName("저장 후 아이디 반환 테스트")
    void save_test() {
        // given
        ReservationTime reservationTime = em.find(ReservationTime.class, 1L);
        Theme theme = em.find(Theme.class, 1L);
        Member member = em.find(Member.class, 1L);
        Reservation reservation = Reservation.createWithoutId(
                LocalDateTime.of(1999, 11, 2, 20, 10), member, LocalDate.of(2000, 11, 2), reservationTime, theme,
                ReservationStatus.RESERVED);
        // when
        Reservation saveReservation = repository.save(reservation);
        // then
        assertThat(saveReservation.getId()).isNotNull();
    }

    @Test
    @DisplayName("날짜와 테마 관련 조회 테스트")
    void find_by_themeGetId_and_getDate() {
        // given
        ReservationTime reservationTime = em.find(ReservationTime.class, 1L);
        Theme theme = em.find(Theme.class, 1L);
        Member member = em.find(Member.class, 1L);
        Reservation reservation1 = Reservation.createWithoutId(LocalDateTime.of(1999, 11, 2, 20, 10), member,
                LocalDate.of(2000, 11, 2), reservationTime, theme, ReservationStatus.RESERVED);
        Reservation reservation2 = Reservation.createWithoutId(LocalDateTime.of(1999, 11, 2, 20, 10), member,
                LocalDate.of(2000, 10, 2), reservationTime, theme, ReservationStatus.RESERVED);
        Reservation reservation3 = Reservation.createWithoutId(LocalDateTime.of(1999, 11, 2, 20, 10), member,
                LocalDate.of(2000, 11, 2), reservationTime, theme, ReservationStatus.RESERVED);
        repository.save(reservation1);
        repository.save(reservation2);
        repository.save(reservation3);
        // when
        List<Reservation> reservations = repository.findByDateAndThemeId(LocalDate.of(2000, 11, 2), 1L);
        // then
        assertThat(reservations).hasSize(2);

    }

    @Test
    @DisplayName("삭제 성공 관련 테스트")
    void delete_test() {
        // given
        ReservationTime reservationTime = em.find(ReservationTime.class, 1L);
        Theme theme = em.find(Theme.class, 1L);
        Member member = em.find(Member.class, 1L);
        Reservation reservation = Reservation.createWithoutId(LocalDateTime.of(1999, 11, 2, 20, 10), member,
                LocalDate.of(2000, 11, 2), reservationTime, theme, ReservationStatus.RESERVED);
        em.persist(reservation);
        em.flush();
        // when & then
        assertDoesNotThrow(() -> repository.deleteById(reservation.getId()));
    }

    @Test
    @DisplayName("전체 조회 테스트")
    void find_all_test() {
        // given
        ReservationTime reservationTime = em.find(ReservationTime.class, 1L);
        Theme theme = em.find(Theme.class, 1L);
        Member member = em.find(Member.class, 1L);
        Reservation reservation1 = Reservation.createWithoutId(LocalDateTime.of(1999, 11, 2, 20, 10), member,
                LocalDate.of(2000, 11, 2), reservationTime, theme, ReservationStatus.RESERVED);
        Reservation reservation2 = Reservation.createWithoutId(LocalDateTime.of(1999, 11, 2, 20, 10), member,
                LocalDate.of(2000, 10, 2), reservationTime, theme, ReservationStatus.RESERVED);
        Reservation reservation3 = Reservation.createWithoutId(LocalDateTime.of(1999, 11, 2, 20, 10), member,
                LocalDate.of(2000, 11, 2), reservationTime, theme, ReservationStatus.RESERVED);
        repository.save(reservation1);
        repository.save(reservation2);
        repository.save(reservation3);
        // when
        List<Reservation> reservations = repository.findAll();
        // then
        assertThat(reservations).hasSize(7);

    }

    @ParameterizedTest
    @DisplayName("예약 시간 유무 조회 테스트")
    @CsvSource({"1,true", "4,false"})
    void exist_by_time(Long timeId, boolean expected) {
        // given
        ReservationTime reservationTime = em.find(ReservationTime.class, 1L);
        Theme theme = em.find(Theme.class, 1L);
        Member member = em.find(Member.class, 1L);
        Reservation reservation = Reservation.createWithoutId(LocalDateTime.of(1999, 11, 2, 20, 10), member,
                LocalDate.of(2000, 11, 2), reservationTime, theme, ReservationStatus.RESERVED);
        em.persist(reservation);
        em.flush();
        // when
        boolean existed = repository.existsByTimeId(timeId);
        // then
        assertThat(existed).isEqualTo(expected);
    }

    @Test
    @DisplayName("특정 조건 조회 테스트")
    void exist_by_test() {
        // given
        ReservationTime reservationTime = em.find(ReservationTime.class, 1L);
        Theme theme = em.find(Theme.class, 1L);
        Member member = em.find(Member.class, 1L);
        Reservation reservation = Reservation.createWithoutId(LocalDateTime.of(1999, 11, 2, 20, 10), member,
                LocalDate.of(2000, 11, 2), reservationTime, theme, ReservationStatus.RESERVED);
        em.persist(reservation);
        em.flush();
        // when & then
        assertAll(
                () -> assertThat(repository.existsByDateAndTimeIdAndThemeIdAndStatus(
                        LocalDate.of(2000, 11, 3), 1L, 1L, ReservationStatus.RESERVED)).isFalse(),
                () -> assertThat(repository.existsByDateAndTimeIdAndThemeIdAndStatus(
                        LocalDate.of(2000, 11, 2), 100L, 1L, ReservationStatus.RESERVED)).isFalse(),
                () -> assertThat(repository.existsByDateAndTimeIdAndThemeIdAndStatus(
                        LocalDate.of(2000, 11, 2), 1L, 2L, ReservationStatus.RESERVED)).isFalse(),
                () -> assertThat(repository.existsByDateAndTimeIdAndThemeIdAndStatus(
                        LocalDate.of(2000, 11, 2), 1L, 1L, ReservationStatus.RESERVED)).isTrue()
        );
    }

    @ParameterizedTest
    @DisplayName("테마 유무 조회 테스트")
    @CsvSource({"3,true", "4,false"})
    void exist_by_theme(Long themeId, boolean expected) {
        // given
        ReservationTime reservationTime = em.find(ReservationTime.class, 1L);
        Theme theme = em.find(Theme.class, 1L);
        Member member = em.find(Member.class, 1L);
        Reservation reservation = Reservation.createWithoutId(LocalDateTime.of(1999, 11, 2, 20, 10), member,
                LocalDate.of(2000, 11, 2), reservationTime, theme, ReservationStatus.RESERVED);
        em.persist(reservation);
        em.flush();
        // when
        boolean existed = repository.existsByThemeId(themeId);
        // then
        assertThat(existed).isEqualTo(expected);
    }

    @ParameterizedTest
    @MethodSource
    @DisplayName("예약 조건 조회 테스트")
    void findByMemberIdAndThemeGetIdAndGetDate_test(Long memberId, Long themeId, LocalDate from, LocalDate to,
                                                    int expectedSize) {
        // when
        List<Reservation> reservations = repository.findByMemberIdAndThemeIdAndDate(memberId, themeId, from, to);
        // then
        assertThat(reservations).hasSize(expectedSize);
    }

    private static Stream<Arguments> findByMemberIdAndThemeGetIdAndGetDate_test() {
        return Stream.of(
                Arguments.of(1L, null, null, null, 3),
                Arguments.of(2L, null, null, null, 1),
                Arguments.of(3L, null, null, null, 0),
                Arguments.of(null, 1L, null, null, 2),
                Arguments.of(null, 2L, null, null, 1),
                Arguments.of(null, 3L, null, null, 1),
                Arguments.of(null, null, LocalDate.of(2025, 4, 19), null, 3),
                Arguments.of(null, null, LocalDate.of(2025, 4, 18), null, 4),
                Arguments.of(null, null, LocalDate.of(2025, 4, 28), null, 2),
                Arguments.of(null, null, null, LocalDate.of(2025, 4, 28), 4),
                Arguments.of(null, null, null, LocalDate.of(2025, 4, 26), 2),
                Arguments.of(null, null, null, LocalDate.of(2025, 4, 18), 1),
                Arguments.of(null, null, null, LocalDate.of(2025, 4, 17), 0),
                Arguments.of(1L, 1L, null, null, 2),
                Arguments.of(1L, null, LocalDate.of(2025, 4, 28), null, 2),
                Arguments.of(1L, null, LocalDate.of(2025, 4, 26), null, 3)
        );
    }

    @ParameterizedTest
    @MethodSource
    @DisplayName("특정 상태의 예약이 존재하는지 확인한다.")
    void existsByDateAndTimeIdAndThemeIdAndStatus_test(LocalDate date, Long timeId, Long themeId,
                                                       ReservationStatus status, boolean expected) {
        // when
        boolean exists = repository.existsByDateAndTimeIdAndThemeIdAndStatus(date, timeId, themeId, status);
        // then
        assertThat(exists).isEqualTo(expected);
    }

    private static Stream<Arguments> existsByDateAndTimeIdAndThemeIdAndStatus_test() {
        return Stream.of(
                Arguments.of(LocalDate.of(2025, 4, 18), 1L, 2L, ReservationStatus.RESERVED, true),
                Arguments.of(LocalDate.of(2025, 4, 26), 1L, 3L, ReservationStatus.RESERVED, true),
                Arguments.of(LocalDate.of(2025, 4, 28), 1L, 1L, ReservationStatus.RESERVED, true),
                Arguments.of(LocalDate.of(2025, 4, 28), 2L, 1L, ReservationStatus.RESERVED, true),
                Arguments.of(LocalDate.of(2025, 4, 18), 1L, 2L, ReservationStatus.WAITED, false),
                Arguments.of(LocalDate.of(2025, 4, 26), 1L, 3L, ReservationStatus.WAITED, false),
                Arguments.of(LocalDate.of(2025, 4, 28), 1L, 1L, ReservationStatus.WAITED, false),
                Arguments.of(LocalDate.of(2025, 4, 28), 2L, 1L, ReservationStatus.WAITED, false),
                Arguments.of(LocalDate.of(2025, 3, 28), 2L, 1L, ReservationStatus.RESERVED, false),
                Arguments.of(LocalDate.of(2025, 4, 28), 3L, 1L, ReservationStatus.RESERVED, false),
                Arguments.of(LocalDate.of(2025, 4, 28), 2L, 2L, ReservationStatus.RESERVED, false)
        );
    }

    @ParameterizedTest
    @MethodSource
    @DisplayName("이미 예약이 존재하는지 확인한다.(존재하는 경우)")
    void existsReservation_test(LocalDate date, Long timeId, Long themeId, Long memberId, ReservationStatus status,
                                boolean expected) {
        // when
        boolean existed = repository.existsReservation(date, timeId, themeId, memberId, status);
        // then
        assertThat(existed).isEqualTo(expected);
    }

    private static Stream<Arguments> existsReservation_test() {
        return Stream.of(
                Arguments.of(LocalDate.of(2025, 4, 18), 1L, 2L, 2L, ReservationStatus.RESERVED, true),
                Arguments.of(LocalDate.of(2025, 4, 26), 1L, 3L, 1L, ReservationStatus.RESERVED, true),
                Arguments.of(LocalDate.of(2025, 4, 28), 1L, 1L, 1L, ReservationStatus.RESERVED, true),
                Arguments.of(LocalDate.of(2025, 4, 28), 2L, 1L, 1L, ReservationStatus.RESERVED, true),
                Arguments.of(LocalDate.of(2025, 4, 18), 1L, 2L, 2L, ReservationStatus.WAITED, false),
                Arguments.of(LocalDate.of(2025, 4, 26), 1L, 3L, 1L, ReservationStatus.WAITED, false),
                Arguments.of(LocalDate.of(2025, 4, 28), 1L, 1L, 1L, ReservationStatus.WAITED, false),
                Arguments.of(LocalDate.of(2025, 4, 28), 2L, 1L, 1L, ReservationStatus.WAITED, false),
                Arguments.of(LocalDate.of(2025, 4, 28), 2L, 1L, 2L, ReservationStatus.RESERVED, false),
                Arguments.of(LocalDate.of(2025, 4, 28), 1L, 1L, 2L, ReservationStatus.RESERVED, false),
                Arguments.of(LocalDate.of(2025, 4, 27), 2L, 1L, 1L, ReservationStatus.RESERVED, false)
        );
    }

    @Test
    @DisplayName("등수와 함께 특정 맴버의 예약 기록을 가져온다.")
    void findReservationWithRank_test() {
        // given
        ReservationTime reservationTime = em.find(ReservationTime.class, 1L);
        Theme theme = em.find(Theme.class, 1L);
        Member member1 = em.find(Member.class, 2L);
        Member member2 = em.find(Member.class, 3L);
        Reservation reservation1 = Reservation.createWithoutId(LocalDateTime.of(2025, 3, 22, 21, 10), member1,
                LocalDate.of(2025, 4, 28), reservationTime, theme, ReservationStatus.WAITED);
        Reservation reservation2 = Reservation.createWithoutId(LocalDateTime.of(2025, 3, 22, 20, 10), member2,
                LocalDate.of(2025, 4, 28), reservationTime, theme, ReservationStatus.WAITED);
        em.persist(reservation1);
        em.persist(reservation2);
        em.flush();
        // when
        List<ReservationWithRank> reservations1 = repository.findReservationWithRankById(2L);
        List<ReservationWithRank> reservations2 = repository.findReservationWithRankById(3L);
        // then
        assertAll(
                () -> assertThat(reservations1).hasSize(2),
                () -> assertThat(reservations1.get(0).getRank()).isEqualTo(0),
                () -> assertThat(reservations1.get(1).getRank()).isEqualTo(2)
        );
        assertAll(
                () -> assertThat(reservations2).hasSize(1),
                () -> assertThat(reservations2.get(0).getRank()).isEqualTo(1)
        );
    }

    @Test
    @DisplayName("모든 미래 예약을 가져온다.")
    void findAllWaitingReservations_test() {
        // given
        ReservationTime reservationTime = em.find(ReservationTime.class, 1L);
        Theme theme1 = em.find(Theme.class, 1L);
        Theme theme2 = em.find(Theme.class, 2L);
        Member member1 = em.find(Member.class, 2L);
        Member member2 = em.find(Member.class, 3L);
        Reservation reservation1 = Reservation.createWithoutId(LocalDateTime.of(2025, 3, 22, 21, 10), member1,
                LocalDate.of(2025, 4, 28), reservationTime, theme1, ReservationStatus.WAITED);
        Reservation reservation2 = Reservation.createWithoutId(LocalDateTime.of(2025, 3, 22, 20, 10), member2,
                LocalDate.of(2025, 4, 28), reservationTime, theme1, ReservationStatus.WAITED);
        Reservation reservation3 = Reservation.createWithoutId(LocalDateTime.of(2025, 3, 22, 20, 15), member2,
                LocalDate.of(2025, 4, 18), reservationTime, theme2, ReservationStatus.WAITED);
        em.persist(reservation1);
        em.persist(reservation2);
        em.persist(reservation3);
        em.flush();
        em.clear();
        LocalDateTime now = LocalDateTime.of(2025, 4, 20, 10, 0);
        // when
        List<Reservation> waitingReservations = repository.findAllWaitingReservations(now);
        // then
        assertAll(
                () -> assertThat(waitingReservations).hasSize(2),
                () -> assertThat(waitingReservations.get(0).getDate()).isEqualTo(LocalDate.of(2025, 4, 28)),
                () -> assertThat(waitingReservations.get(1).getDate()).isEqualTo(LocalDate.of(2025, 4, 28))
        );
    }

    @Test
    @DisplayName("정상적으로 예약 상태를 바꾼다.")
    void changeReservationStatus_test() {
        // given
        ReservationTime reservationTime = em.find(ReservationTime.class, 1L);
        Theme theme = em.find(Theme.class, 1L);
        Member member = em.find(Member.class, 2L);
        Reservation reservation = Reservation.createWithoutId(LocalDateTime.of(2025, 3, 22, 21, 10), member,
                LocalDate.of(2025, 4, 28), reservationTime, theme, ReservationStatus.WAITED);
        em.persist(reservation);
        em.flush();
        em.clear();
        // when
        repository.changeReservationStatus(5L, ReservationStatus.RESERVED);
        // then
        Reservation findReservation = em.find(Reservation.class, 5L);
        assertThat(findReservation.getStatus()).isEqualTo(ReservationStatus.RESERVED);
    }
}