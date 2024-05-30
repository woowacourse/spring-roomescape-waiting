package roomescape.reservation.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static roomescape.reservation.repository.fixture.ReservationTimeFixture.TIME1;

import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.repository.fixture.ReservationTimeFixture;

@DataJpaTest
class ReservationTimeRepositoryTest {

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @DisplayName("모든 예약 시간을 조회한다.")
    @Test
    void findAll() {
        final var result = reservationTimeRepository.findAll();

        assertThat(result).hasSize(ReservationTimeFixture.count());
    }

    @DisplayName("id로 예약 시간을 조회한다.")
    @Test
    void findById() {
        final var result = reservationTimeRepository.findById(1L);

        assertThat(result.get()).isEqualTo(TIME1.create());
    }

    @DisplayName("예약 시간을 생성한다.")
    @Test
    void save() {
        final var reservationTime = new ReservationTime(LocalTime.parse("00:30"));

        reservationTimeRepository.save(reservationTime);

        assertThat(reservationTimeRepository.findAll()).hasSize(ReservationTimeFixture.count() + 1);
    }

    @DisplayName("id로 예약 시간을 삭제한다.")
    @Test
    void deleteById() {
        reservationTimeRepository.deleteById(4L);

        assertThat(reservationTimeRepository.findById(4L)).isEmpty();
    }
}
