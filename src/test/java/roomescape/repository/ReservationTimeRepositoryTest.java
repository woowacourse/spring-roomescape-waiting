package roomescape.repository;

import java.time.LocalTime;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.model.ReservationTime;

@DataJpaTest
class ReservationTimeRepositoryTest {

    @Autowired
    ReservationTimeRepository reservationTimeRepository;

    @Test
    @DisplayName("시각을 이용해 예약 시각의 존재 여부를 확인한다")
    void test1() {
        // given
        LocalTime startAt = LocalTime.of(12, 30);
        ReservationTime reservationTime = new ReservationTime(startAt);
        ReservationTime savedReservationTime = reservationTimeRepository.save(reservationTime);

        // when
        boolean actual = reservationTimeRepository.existsByStartAt(startAt);

        // then
        assertThat(actual).isTrue();
    }

}
