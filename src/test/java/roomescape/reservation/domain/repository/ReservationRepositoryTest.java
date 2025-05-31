package roomescape.reservation.domain.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import roomescape.reservation.domain.Reservation;

@ActiveProfiles("test")
@DataJpaTest
public class ReservationRepositoryTest {

    @Autowired
    private ReservationRepository reservationRepository;

    @DisplayName("날짜와 테마 아이디를 통해 예약을 가져올 수 있어야 한다.")
    @Test
    void get_reservation_from_date_and_theme_id() {
        LocalDate dummyDate = LocalDate.now().minusDays(3);
        List<Reservation> findReservation = reservationRepository.findByDateAndThemeId(dummyDate,
            1L);
        assertThat(findReservation).isNotEmpty();
        assertThat(findReservation.getFirst().getDate()).isEqualTo(dummyDate);
        assertThat(findReservation.getFirst().getTheme().getId()).isEqualTo(1L);
    }

    @DisplayName("테마 아이디, 멤버 아이디, 기간으로 예약을 조회할 수 있어야 한다.")
    @Test
    void find_by_theme_id_and_member_id_and_date_between() {
        // given
        Long themeId = 2L;
        Long memberId = 1L;
        LocalDate start = LocalDate.now().minusDays(5);
        LocalDate end = LocalDate.now().minusDays(1);

        // when
        List<Reservation> reservations = reservationRepository.findByThemeIdAndMemberIdAndDateBetween(
            themeId, memberId, start, end);

        // then
        assertThat(reservations).hasSize(4);
    }

    @DisplayName("시간 아이디로 예약 존재 여부를 확인할 수 있어야 한다.")
    @Test
    void exists_by_time_id() {
        // given
        Long existingTimeId = 1L;
        Long nonExistingTimeId = 6L;

        // when & then
        assertThat(reservationRepository.existsByTimeId(existingTimeId)).isTrue();
        assertThat(reservationRepository.existsByTimeId(nonExistingTimeId)).isFalse();
    }

    @DisplayName("테마 아이디로 예약 존재 여부를 확인할 수 있어야 한다.")
    @Test
    void exists_by_theme_id() {
        // given
        Long existingThemeId = 1L;
        Long nonExistingThemeId = 99L;

        // when & then
        assertThat(reservationRepository.existsByThemeId(existingThemeId)).isTrue();
        assertThat(reservationRepository.existsByThemeId(nonExistingThemeId)).isFalse();
    }

    @DisplayName("멤버 아이디, 테마 아이디, 예약 시간 아이디, 날짜로 예약 존재 여부를 확인할 수 있어야 한다.")
    @Test
    void exists_by_member_id_and_theme_id_and_reservation_time_id_and_date() {
        // given
        Long memberId = 1L;
        Long themeId = 2L;
        Long reservationTimeId = 1L;
        LocalDate date = LocalDate.now().minusDays(3);

        // when & then
        assertThat(reservationRepository.existsByMemberIdAndThemeIdAndTimeIdAndDate(
            memberId, themeId, reservationTimeId, date)).isTrue();

        // Non-existing combination
        assertThat(reservationRepository.existsByMemberIdAndThemeIdAndTimeIdAndDate(
            memberId, 3L, reservationTimeId, date)).isFalse();
    }

    @DisplayName("멤버 아이디로 예약을 조회할 수 있어야 한다.")
    @Test
    void find_by_member_id() {
        // given
        Long memberId = 1L;

        // when
        List<Reservation> reservations = reservationRepository.findByMemberId(memberId);

        // then
        assertThat(reservations).hasSize(4);
    }
}
