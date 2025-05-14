package roomescape.fixture;

import java.time.LocalTime;
import org.springframework.stereotype.Component;
import roomescape.time.domain.ReservationTime;
import roomescape.time.service.out.ReservationTimeRepository;

@Component
public class ReservationTimeDbFixture {

    private final ReservationTimeRepository reservationTimeRepository;

    public ReservationTimeDbFixture(ReservationTimeRepository reservationTimeRepository) {
        this.reservationTimeRepository = reservationTimeRepository;
    }

    public ReservationTime 열시() {
        ReservationTime reservationTime = ReservationTime.open(LocalTime.of(10, 0));
        return reservationTimeRepository.save(reservationTime);
    }

    public ReservationTime 열한시() {
        ReservationTime reservationTime = ReservationTime.open(LocalTime.of(11, 0));
        return reservationTimeRepository.save(reservationTime);
    }
}
