package roomescape.reservationTime.domain.respository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import roomescape.reservationTime.domain.ReservationTime;

@ActiveProfiles("test")
@DataJpaTest
public class ReservationTimeRepositoryTest {

    @Autowired
    private ReservationTimeRepository timeRepository;

    @DisplayName("예약 시간을 저장한다")
    @Test
    void save() {
        // given
        LocalTime startAt = LocalTime.now();
        ReservationTime reservationTime = new ReservationTime(startAt);

        // when
        timeRepository.save(reservationTime);
        Iterable<ReservationTime> times = timeRepository.findAll();

        // then
        assertThat(times).extracting(ReservationTime::getStartAt).containsOnly(startAt);
    }
}
