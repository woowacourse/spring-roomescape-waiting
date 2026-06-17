package roomescape.infra;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.domain.ReservationTime;
import roomescape.repository.ReservationTimeRepository;

@DataJpaTest
class JdbcReservationTimeRepositoryTest {

    private static final LocalTime TEN = LocalTime.of(10, 0);
    private static final LocalTime TWELVE = LocalTime.of(12, 0);

    @Autowired
    private ReservationTimeRepository timeRepository;

    @Test
    void 예약시간을_저장한다() {
        ReservationTime tenClock = new ReservationTime(TEN);
        Long saveId = timeRepository.save(tenClock).getId();

        Optional<ReservationTime> reservationTime = timeRepository.findById(saveId);

        assertThat(reservationTime).isPresent();

        assertThat(reservationTime.get().getId()).isNotNull();
        assertThat(reservationTime.get().getStartAt()).isEqualTo(TEN);
    }

    @Test
    void 모든_시간을_오름차순으로_조회한다() {
        ReservationTime twelveClock = new ReservationTime(TWELVE);
        timeRepository.save(twelveClock);

        ReservationTime tenClock = new ReservationTime(TEN);
        Long tenClockSaveId = timeRepository.save(tenClock).getId();

        List<ReservationTime> reservationTimes = timeRepository.findAll();

        assertThat(reservationTimes).hasSize(2);

        ReservationTime firstResult = reservationTimes.getFirst();
        assertThat(firstResult.getId()).isEqualTo(tenClockSaveId);
        assertThat(firstResult.getStartAt()).isEqualTo(TEN);
    }

    @Test
    void 예약시간_id_존재여부() {
        ReservationTime twelveClock = new ReservationTime(TWELVE);
        Long saveId = timeRepository.save(twelveClock).getId();

        assertThat(timeRepository.existsById(saveId)).isTrue();
        assertThat(timeRepository.existsById(999L)).isFalse();
    }

    @Test
    void 예약시간을_삭제한다() {
        ReservationTime twelveClock = new ReservationTime(TWELVE);
        Long saveId = timeRepository.save(twelveClock).getId();

        timeRepository.deleteById(saveId);

        assertThat(timeRepository.findById(saveId)).isEmpty();
    }
}
