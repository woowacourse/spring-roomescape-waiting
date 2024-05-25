package roomescape.helper.domain;

import java.time.LocalTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.reservationtime.ReservationTimeRepository;

@Component
public class ReservationTimeFixture {
    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    public ReservationTime createFutureTime() {
        ReservationTime reservationTime = new ReservationTime(LocalTime.of(2, 30));
        return reservationTimeRepository.save(reservationTime);
    }

    public ReservationTime createPastTime() {
        ReservationTime reservationTime = new ReservationTime(LocalTime.of(1, 30));
        return reservationTimeRepository.save(reservationTime);
    }

    public List<ReservationTime> findAllTime() {
        return reservationTimeRepository.findAll();
    }
}
