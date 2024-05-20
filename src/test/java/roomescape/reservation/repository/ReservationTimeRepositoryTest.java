package roomescape.reservation.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.reservation.domain.ReservationTime;

@DataJpaTest
class ReservationTimeRepositoryTest {

    private static final int DEFAULT_RESERVATION_TIME_COUNT = 4;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @DisplayName("모든 예약 시간을 조회한다.")
    @Test
    void findAll() {
        final var result = reservationTimeRepository.findAll();

        assertThat(result).hasSize(DEFAULT_RESERVATION_TIME_COUNT);
    }

    @DisplayName("id로 예약 시간을 조회한다.")
    @Test
    void findById() {
        final var result = reservationTimeRepository.findById(1L);

        assertThat(result.get().getId()).isEqualTo(1L);
    }

    @DisplayName("예약 시간을 생성한다.")
    @Test
    void save() {
        final var reservationTime = new ReservationTime(LocalTime.parse("00:30"));

        reservationTimeRepository.save(reservationTime);

        assertThat(reservationTimeRepository.findAll()).hasSize(DEFAULT_RESERVATION_TIME_COUNT + 1);
    }

    @DisplayName("id로 예약 시간을 삭제한다.")
    @Test
    void deleteById() {
        final var result = reservationTimeRepository.deleteById(4);

        assertAll(
                () -> assertThat(result).isEqualTo(1),
                () -> assertThat(reservationTimeRepository.findAll())
                        .extracting(ReservationTime::getId)
                        .doesNotContain(4L)
        );
    }
}
