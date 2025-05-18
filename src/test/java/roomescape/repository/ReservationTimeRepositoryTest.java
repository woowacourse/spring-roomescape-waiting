package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.model.ReservationTime;

@DataJpaTest
class ReservationTimeRepositoryTest {

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @DisplayName("해당 시간이 존재하면 true를 반환한다.")
    @Test
    void existsByStartAt() {
        //given
        ReservationTime reservationTime = new ReservationTime(LocalTime.of(11, 0));
        reservationTimeRepository.save(reservationTime);

        //when
        boolean actual = reservationTimeRepository.existsByStartAt(reservationTime.getStartAt());

        //then
        assertThat(actual).isTrue();
    }

    @DisplayName("해당 시간이 존재하지 않으면 false를 반환한다.")
    @Test
    void nonExistsByStartAt() {
        //given
        ReservationTime reservationTime = new ReservationTime(LocalTime.of(11, 0));
        reservationTimeRepository.save(reservationTime);

        LocalTime otherTime = LocalTime.of(10, 10);

        //when
        boolean actual = reservationTimeRepository.existsByStartAt(otherTime);

        //then
        assertThat(actual).isFalse();
    }

}
