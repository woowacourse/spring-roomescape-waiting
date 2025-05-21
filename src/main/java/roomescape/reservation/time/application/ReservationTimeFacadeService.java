package roomescape.reservation.time.application;

import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.application.service.ReservationQueryService;
import roomescape.reservation.time.application.service.ReservationTimeCommandService;
import roomescape.reservation.time.application.service.ReservationTimeQueryService;
import roomescape.reservation.time.domain.ReservationTime;
import roomescape.reservation.time.presentation.dto.ReservationTimeRequest;
import roomescape.reservation.time.presentation.dto.ReservationTimeResponse;
import roomescape.reservation.time.presentation.dto.TimeConditionRequest;
import roomescape.reservation.time.presentation.dto.TimeConditionResponse;

@Service
public class ReservationTimeFacadeService {

    private final ReservationTimeQueryService reservationTimeQueryService;
    private final ReservationTimeCommandService reservationTimeCommandService;
    private final ReservationQueryService reservationQueryService;

    public ReservationTimeFacadeService(ReservationTimeQueryService reservationTimeQueryService,
                                        ReservationTimeCommandService reservationTimeCommandService,
                                        ReservationQueryService reservationQueryService) {
        this.reservationTimeQueryService = reservationTimeQueryService;
        this.reservationTimeCommandService = reservationTimeCommandService;
        this.reservationQueryService = reservationQueryService;
    }

    public ReservationTimeResponse createReservationTime(final ReservationTimeRequest request) {
        ReservationTime reservationTime = reservationTimeCommandService.save(request);
        return ReservationTimeResponse.from(reservationTime);
    }

    public void deleteReservationTimeById(final Long id) {
        reservationTimeCommandService.deleteById(id);
    }

    public List<ReservationTimeResponse> getReservationTimes() {
        return reservationTimeQueryService.findAll().stream()
            .map(ReservationTimeResponse::from)
            .toList();
    }

    public List<TimeConditionResponse> getTimesWithCondition(final TimeConditionRequest request) {
        List<Reservation> reservations = reservationQueryService.findByThemeIdAndDate(request.themeId(), request.date());
        List<ReservationTime> times = reservationTimeQueryService.findAll();

        return times.stream().map(time -> {
            boolean hasTime = reservations.stream()
                .anyMatch(reservation -> reservation.isSameTime(time));
            return new TimeConditionResponse(time.getId(), time.getStartAt(), hasTime);
        }).toList();
    }
}
