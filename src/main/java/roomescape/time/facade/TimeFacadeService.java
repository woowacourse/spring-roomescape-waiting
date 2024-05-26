package roomescape.time.facade;

import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.reservation.service.ReservationService;
import roomescape.time.domain.Time;
import roomescape.time.dto.TimeRequest;
import roomescape.time.dto.TimeResponse;
import roomescape.time.service.TimeService;

@Service
public class TimeFacadeService {

    private final ReservationService reservationService;
    private final TimeService timeService;

    public TimeFacadeService(ReservationService reservationService, TimeService timeService) {
        this.reservationService = reservationService;
        this.timeService = timeService;
    }

    public TimeResponse addTime(TimeRequest timeRequest) {
        Time time = timeService.addReservationTime(timeRequest);
        return new TimeResponse(time.getId(), time.getStartAt());
    }

    public List<TimeResponse> findTimes() {
        List<Time> times = timeService.findReservationTimes();

        return times.stream()
                .map(time -> new TimeResponse(time.getId(), time.getStartAt()))
                .toList();
    }

    public void removeTime(long timeId) {
        reservationService.validateReservationExistence(timeId);
        timeService.removeReservationTime(timeId);
    }
}
