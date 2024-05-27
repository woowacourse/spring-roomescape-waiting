package roomescape.service.booking.time;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.dto.reservationtime.ReservationTimeRequest;
import roomescape.dto.reservationtime.ReservationTimeResponse;
import roomescape.dto.reservationtime.TimeWithAvailableResponse;
import roomescape.service.booking.time.module.TimeDeleteService;
import roomescape.service.booking.time.module.TimeRegisterService;
import roomescape.service.booking.time.module.TimeSearchService;

@Service
public class ReservationTimeService {

    private final TimeRegisterService timeRegisterService;
    private final TimeSearchService timeSearchService;
    private final TimeDeleteService timeDeleteService;

    public ReservationTimeService(TimeRegisterService timeRegisterService,
                                  TimeSearchService timeSearchService,
                                  TimeDeleteService timeDeleteService
    ) {
        this.timeRegisterService = timeRegisterService;
        this.timeSearchService = timeSearchService;
        this.timeDeleteService = timeDeleteService;
    }

    public Long resisterReservationTime(ReservationTimeRequest reservationTimeRequest) {
        return timeRegisterService.registerTime(reservationTimeRequest);
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
