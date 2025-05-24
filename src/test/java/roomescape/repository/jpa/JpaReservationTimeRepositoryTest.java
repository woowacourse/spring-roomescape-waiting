package roomescape.repository.jpa;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.entity.ReservationTime;

@DataJpaTest
class JpaReservationTimeRepositoryTest {

    @Autowired
    private JpaReservationTimeRepository reservationTimeRepository;

    @Test
    @DisplayName("모든 시간대를 조회할 수 있다.")
    void findAllOrderByStartAtAsc() {
        LocalTime time1 = LocalTime.of(10, 0);
        LocalTime time2 = LocalTime.of(11, 0);
        LocalTime time3 = LocalTime.of(12, 0);

        reservationTimeRepository.save(new ReservationTime(time1));
        reservationTimeRepository.save(new ReservationTime(time2));
        reservationTimeRepository.save(new ReservationTime(time3));

        assertThat(reservationTimeRepository.findAllByOrderByStartAtAsc()).hasSize(3);
    }

    @Test
    @DisplayName("해당 시간이 있다면 true를 반환한다.")
    void existTimeByStartAt() {
        LocalTime time = LocalTime.of(10, 0);

        reservationTimeRepository.save(new ReservationTime(time));

        assertThat(reservationTimeRepository.existsByStartAt(time)).isTrue();
    }

    @Test
    @DisplayName("해당 시간이 없다면 false를 반환한다.")
    void notExistTimeByStartAt() {
        LocalTime time = LocalTime.of(11, 0);

        assertThat(reservationTimeRepository.existsByStartAt(time)).isFalse();
    }
}
