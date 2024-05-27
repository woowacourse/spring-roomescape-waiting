package roomescape.service.booking.time;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.dto.reservationtime.ReservationTimeRequest;
import roomescape.dto.reservationtime.ReservationTimeResponse;
import roomescape.dto.reservationtime.TimeWithAvailableResponse;
import roomescape.service.booking.time.module.TimeDeleteService;
import roomescape.service.booking.time.module.TimeResisterService;
import roomescape.service.booking.time.module.TimeSearchService;

@Service
public class ReservationTimeService {

    private final TimeResisterService timeResisterService;
    private final TimeSearchService timeSearchService;
    private final TimeDeleteService timeDeleteService;

    public ReservationTimeService(TimeResisterService timeResisterService,
                                  TimeSearchService timeSearchService,
                                  TimeDeleteService timeDeleteService
    ) {
        this.timeResisterService = timeResisterService;
        this.timeSearchService = timeSearchService;
        this.timeDeleteService = timeDeleteService;
    }

    public Long resisterReservationTime(ReservationTimeRequest reservationTimeRequest) {
        return timeResisterService.registerTime(reservationTimeRequest);
    }

    public ReservationTimeResponse findReservationTime(Long timeId) {
        return timeSearchService.findTime(timeId);
    }

    public List<ReservationTimeResponse> findAllReservationTimes() {
        return timeSearchService.findAllTimes();
    }

    public List<TimeWithAvailableResponse> getAvailableTimes(LocalDate date, Long themeId) {
        return timeSearchService.findAvailableTimes(date, themeId);
    }

    public void deleteReservationTime(Long timeId) {
        timeDeleteService.deleteTime(timeId);
    }
}
