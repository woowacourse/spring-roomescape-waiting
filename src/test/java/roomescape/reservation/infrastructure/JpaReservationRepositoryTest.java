package roomescape.reservation.infrastructure;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.reservation.domain.Reservation;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class JpaReservationRepositoryTest {

    @Autowired
    private JpaReservationRepository jpaReservationRepository;

    @Test
    void 특정_날짜와_테마로_예약을_조회할_수_있다() {
        List<Reservation> result = jpaReservationRepository.findByDateAndThemeId(
            LocalDate.of(2025, 4, 28), 1L);

        assertThat(result)
            .extracting(Reservation::getId)
            .containsExactly(1L, 2L);
    }

    @Test
    void 회원_ID_테마_ID_날짜_범위로_예약을_조회할_수_있다() {
        LocalDate from = LocalDate.of(2025, 4, 1);
        LocalDate to = LocalDate.of(2025, 4, 30);

        List<Reservation> result = jpaReservationRepository.findByMemberIdAndThemeIdAndDateBetween(1L, 1L, from, to);

        assertThat(result)
            .extracting(Reservation::getId)
            .containsExactly(1L, 2L);
    }

    @Test
    void 테마_ID와_날짜로_예약을_조회할_수_있다() {
        LocalDate date = LocalDate.of(2025, 4, 28);

        List<Reservation> result = jpaReservationRepository.findByThemeIdAndDate(1L, date);
        assertThat(result)
            .extracting(Reservation::getId)
            .containsExactly(1L, 2L);
    }

    @Test
    void 예약시간_ID로_예약_존재여부를_확인할_수_있다() {
        boolean exists = jpaReservationRepository.existsByTimeId(1L);

        assertThat(exists).isTrue();
    }

    @Test
    void 테마_ID로_예약_존재여부를_확인할_수_있다() {
        boolean exists = jpaReservationRepository.existsByThemeId(1L);

        assertThat(exists).isTrue();
    }

    @Test
    void 정확한_날짜_시간_테마로_예약을_조회할_수_있다() {
        LocalDate date = LocalDate.of(2025, 4, 28);

        Optional<Reservation> result = jpaReservationRepository.findByDateAndTimeIdAndThemeId(
            date, 1L, 1L);
        assertThat(result)
            .map(Reservation::getId)
            .contains(1L);
    }

    @Test
    void 회원_ID로_예약을_조회할_수_있다() {
        List<Reservation> result = jpaReservationRepository.findByMemberId(1L);

        assertThat(result)
            .extracting(Reservation::getId)
            .containsExactly(1L, 2L);
    }

    @Test
    void 모든_예약을_조회할_수_있다() {
        List<Reservation> result = jpaReservationRepository.findAll();

        assertThat(result)
            .extracting(Reservation::getId)
            .containsExactly(1L, 2L, 3L, 4L);
    }
}