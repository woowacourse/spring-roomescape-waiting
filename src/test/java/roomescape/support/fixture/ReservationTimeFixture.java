package roomescape.support.fixture;

import org.springframework.stereotype.Component;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.repository.ReservationTimeRepository;

import java.time.LocalTime;
import java.util.List;

@Component
public class ReservationTimeFixture {

    private final ReservationTimeRepository reservationTimeRepository;

    public ReservationTimeFixture(ReservationTimeRepository reservationTimeRepository) {
        this.reservationTimeRepository = reservationTimeRepository;
    }

    public ReservationTime createTime() {
        return reservationTimeRepository.save(new ReservationTime(LocalTime.of(12, 30)));
    }

    public ReservationTime createTime(LocalTime time) {
        return reservationTimeRepository.save(new ReservationTime(time));
    }

    public List<ReservationTime> findAll() {
        return reservationTimeRepository.findAll();
    }
}
