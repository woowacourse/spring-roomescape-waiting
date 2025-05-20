package roomescape.reservation.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
import roomescape.reservationTime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

@Sql(scripts = "/data/reservationConditionTest.sql")
@DataJpaTest
class JpaReservationRepositoryTest {

    @Autowired
    private JpaReservationRepository repository;

    @Autowired
    private EntityManager em;

    private static Stream<Arguments> findByMemberIdAndThemeIdAndDate_test() {
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

    @Test
    @DisplayName("저장 후 아이디 반환 테스트")
    void save_test() {
        // given
        ReservationTime reservationTime = em.find(ReservationTime.class, 1L);
        Theme theme = em.find(Theme.class, 1L);
        Member member = em.find(Member.class, 1L);
        Reservation reservation = Reservation.createWithoutId(
                LocalDateTime.of(1999, 11, 2, 20, 10), member, LocalDate.of(2000, 11, 2), reservationTime, theme);
        // when
        Reservation saveReservation = repository.save(reservation);
        // then
        assertThat(saveReservation.getId()).isNotNull();
    }

    @Test
    @DisplayName("날짜와 테마 관련 조회 테스트")
    void find_by_themeId_and_date() {
        // given
        ReservationTime reservationTime = em.find(ReservationTime.class, 1L);
        Theme theme = em.find(Theme.class, 1L);
        Member member = em.find(Member.class, 1L);
        Reservation reservation1 = Reservation.createWithoutId(LocalDateTime.of(1999, 11, 2, 20, 10), member,
                LocalDate.of(2000, 11, 2), reservationTime, theme);
        Reservation reservation2 = Reservation.createWithoutId(LocalDateTime.of(1999, 11, 2, 20, 10), member,
                LocalDate.of(2000, 10, 2), reservationTime, theme);
        Reservation reservation3 = Reservation.createWithoutId(LocalDateTime.of(1999, 11, 2, 20, 10), member,
                LocalDate.of(2000, 11, 2), reservationTime, theme);
        repository.save(reservation1);
        repository.save(reservation2);
        repository.save(reservation3);
        // when
        List<Reservation> reservations = repository.findByDateAndTheme_Id(LocalDate.of(2000, 11, 2), 1L);
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
                LocalDate.of(2000, 11, 2), reservationTime, theme);
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
                LocalDate.of(2000, 11, 2), reservationTime, theme);
        Reservation reservation2 = Reservation.createWithoutId(LocalDateTime.of(1999, 11, 2, 20, 10), member,
                LocalDate.of(2000, 10, 2), reservationTime, theme);
        Reservation reservation3 = Reservation.createWithoutId(LocalDateTime.of(1999, 11, 2, 20, 10), member,
                LocalDate.of(2000, 11, 2), reservationTime, theme);
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
                LocalDate.of(2000, 11, 2), reservationTime, theme);
        em.persist(reservation);
        em.flush();
        // when
        boolean existed = repository.existsByTime_Id(timeId);
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
                LocalDate.of(2000, 11, 2), reservationTime, theme);
        em.persist(reservation);
        em.flush();
        // when & then
        assertAll(
                () -> assertThat(repository.existsByDateAndTime_StartAtAndTheme_Id(
                        LocalDate.of(2000, 11, 3), LocalTime.of(10, 0), 1L)).isFalse(),
                () -> assertThat(repository.existsByDateAndTime_StartAtAndTheme_Id(
                        LocalDate.of(2000, 11, 2), LocalTime.of(10, 1), 1L)).isFalse(),
                () -> assertThat(repository.existsByDateAndTime_StartAtAndTheme_Id(
                        LocalDate.of(2000, 11, 2), LocalTime.of(10, 0), 2L)).isFalse(),
                () -> assertThat(repository.existsByDateAndTime_StartAtAndTheme_Id(
                        LocalDate.of(2000, 11, 2), LocalTime.of(10, 0), 1L)).isTrue()
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
                LocalDate.of(2000, 11, 2), reservationTime, theme);
        em.persist(reservation);
        em.flush();
        // when
        boolean existed = repository.existsByTheme_Id(themeId);
        // then
        assertThat(existed).isEqualTo(expected);
    }

    @ParameterizedTest
    @MethodSource
    @DisplayName("예약 조건 조회 테스트")
    void findByMemberIdAndThemeIdAndDate_test(Long memberId, Long themeId, LocalDate from, LocalDate to,
                                              int expectedSize) {
        // when
        List<Reservation> reservations = repository.findByMemberIdAndThemeIdAndDate(memberId, themeId, from, to);
        // then
        assertThat(reservations).hasSize(expectedSize);
    }

    @Test
    @DisplayName("본인 예약들을 조회한다.")
    void findByMemberId_test() {
        // when
        List<Reservation> findReservations = repository.findByMember_Id(1L);
        // then
        assertThat(findReservations).hasSize(3);
        assertThat(findReservations.get(0).getName()).isEqualTo("코기");
        assertThat(findReservations.get(1).getName()).isEqualTo("코기");
        assertThat(findReservations.get(2).getName()).isEqualTo("코기");
    }
}