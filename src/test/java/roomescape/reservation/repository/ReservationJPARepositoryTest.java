package roomescape.reservation.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.jdbc.Sql;
import roomescape.reservation.domain.Reservation;

@DataJpaTest
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
@Sql({"/test-time-data.sql", "/test-theme-data.sql", "/test-member-data.sql", "/test-reservation-data.sql"})
public class ReservationJPARepositoryTest {

    @Autowired
    private ReservationRepository reservationRepository;

    @DisplayName("모든 예약 목록을 조회할 수 있다.")
    @Test
    void testFindAll() {
        // given
        // when
        List<Reservation> reservations = reservationRepository.findAll();
        // then
        assertThat(reservations).hasSize(3);
    }

    @DisplayName("멤버 ID, 테마 ID, 해당 기간에 맞는 예약을 조회할 수 있다.")
    @Test
    void testFindAllByMemberIdAndThemeIdAndDateBetween() {
        // given
        // when
        List<Reservation> reservations = reservationRepository.findAllByMemberIdAndThemeIdAndDateBetween(
                1, 1, LocalDate.of(2025, 4, 1), LocalDate.of(2025, 5, 2));
        // then
        assertThat(reservations).hasSize(2);
    }

    @DisplayName("멤버 ID에 맞는 예약을 날짜 내림차순으로 조회할 수 있다.")
    @Test
    void testFindAllByMemberIdOrderByDateDesc() {
        // given
        // when
        List<Reservation> reservations = reservationRepository.findAllByMemberIdOrderByDateDesc(1);
        // then
        assertThat(reservations.getFirst().getDate()).isAfter(reservations.get(1).getDate());
    }

    @DisplayName("해당 기간에 맞는 예약을 조회할 수 있다.")
    @Test
    void testFindByDateBetween() {
        // given
        // when
        List<Reservation> reservations = reservationRepository.findByDateBetween(LocalDate.of(2025, 5, 1),
                LocalDate.of(2025, 5, 3));
        // then
        assertThat(reservations).hasSize(3);
    }

    @DisplayName("시간 ID에 맞는 예약이 존재하는 지 확인할 수 있다.")
    @Test
    void testExistByTimeId() {
        // given
        // when
        // then
        assertThat(reservationRepository.existsByTimeId(1)).isTrue();
        assertThat(reservationRepository.existsByTimeId(4)).isFalse();
    }

    @DisplayName("테마 ID에 맞는 예약이 존재하는 지 확인할 수 있다.")
    @Test
    void testExistByThemeId() {
        // given
        // when
        // then
        assertThat(reservationRepository.existsByThemeId(1)).isTrue();
        assertThat(reservationRepository.existsByThemeId(4)).isFalse();
    }

    @DisplayName("날짜, 시간 ID, 테마 ID에 맞는 예약이 존재하는 지 확인할 수 있다.")
    @Test
    void testExistByDateAndTimeIdAndThemeId() {
        // given
        // when
        // then
        assertThat(reservationRepository.existsByDateAndTimeIdAndThemeId(LocalDate.of(2025, 5, 1), 1, 1)).isTrue();
        assertThat(reservationRepository.existsByDateAndTimeIdAndThemeId(LocalDate.of(2025, 5, 5), 1, 1)).isFalse();
    }
}
