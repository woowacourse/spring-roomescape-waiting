package roomescape.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import roomescape.controller.reservation.dto.ReservationSearchCondition;
import roomescape.domain.Reservation;
import roomescape.domain.Theme;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ReservationRepositoryTest {

    @Autowired
    private ReservationRepository reservationRepository;

    @Test
    @DisplayName("모든 예약 목록을 조회한다.")
    void findAll() {
        // given
        final List<Reservation> expected = List.of(
                new Reservation(1L, null, null, null, null),
                new Reservation(2L, null, null, null, null),
                new Reservation(3L, null, null, null, null),
                new Reservation(4L, null, null, null, null),
                new Reservation(5L, null, null, null, null),
                new Reservation(6L, null, null, null, null)
        );

        assertThat(reservationRepository.findAll()).isEqualTo(expected);
    }

    @Test
    @DisplayName("존재하지 않는 예약을 조회할 경우 빈 값을 반환한다.")
    void findByIdNotPresent() {
        // given
        long id = 100L;

        // when
        Optional<Reservation> actual = reservationRepository.findById(id);

        // then
        assertThat(actual).isEmpty();
    }

    @Test
    @DisplayName("등록되지 않은 시간 아이디로 예약 존재 여부를 확인한다,")
    void existsByTimeIdNotPresent() {
        // given
        final long timeId = 100L;
        final boolean expected = false;

        // when
        final boolean actual = reservationRepository.existsByTimeId(timeId);

        // then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    @DisplayName("유명 테마 검색")
    void findPopularTheme() {
        //given
        final LocalDate now = LocalDate.now();
        final List<Theme> expected = List.of(
                new Theme(2L, null, null, null),
                new Theme(1L, null, null, null),
                new Theme(3L, null, null, null)
        );
        //when
        final List<Theme> popularThemes = reservationRepository
                .findPopularThemes(now.minusDays(8), now.minusDays(1), 10);
        //then
        assertThat(popularThemes).isEqualTo(expected);
    }

    @Test
    @DisplayName("조건에 따른 예약 조회")
    void searchReservations() {
        //given
        final LocalDate now = LocalDate.now();
        final ReservationSearchCondition condition = new ReservationSearchCondition(1L, 1L,
                now.minusDays(7), now.minusDays(1));
        final List<Reservation> expected = List.of(new Reservation(1L, null, null, null, null));
        //when
        final List<Reservation> reservations = reservationRepository.searchReservations(condition);
        //then
        assertThat(reservations).isEqualTo(expected);
    }
}
