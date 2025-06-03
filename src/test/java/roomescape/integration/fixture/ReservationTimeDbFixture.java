package roomescape.integration.fixture;

import java.time.LocalTime;
import org.springframework.stereotype.Component;
import roomescape.domain.time.ReservationTime;
import roomescape.repository.ReservationTimeRepository;

@Component
public class ReservationTimeDbFixture {

    private final ReservationTimeRepository reservationTimeRepository;

    public ReservationTimeDbFixture(final ReservationTimeRepository reservationTimeRepository) {
        this.reservationTimeRepository = reservationTimeRepository;
    }

    public ReservationTime 예약시간_10시() {
        return 예약시간(LocalTime.of(10, 0));
    }

    public ReservationTime 예약시간_11시() {
        return 예약시간(LocalTime.of(11, 0));
    }

    public ReservationTime 예약시간(final LocalTime startAt) {
        return reservationTimeRepository.save(new ReservationTime(null, startAt));
    }
}
