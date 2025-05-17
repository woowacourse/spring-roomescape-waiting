package roomescape.fixture;

import org.springframework.stereotype.Component;
import roomescape.domain.ReservationTime;
import roomescape.infrastructure.repository.ReservationTimeRepository;

import java.time.LocalTime;

@Component
public class ReservationTimeDbFixture {

    private final ReservationTimeRepository reservationTimeRepository;

    public ReservationTimeDbFixture(ReservationTimeRepository reservationTimeRepository) {
        this.reservationTimeRepository = reservationTimeRepository;
    }

    public ReservationTime 예약시간_10시() {
        LocalTime startAt = LocalTime.of(10, 0);

        return reservationTimeRepository.save(ReservationTime.create(startAt));
    }
}
