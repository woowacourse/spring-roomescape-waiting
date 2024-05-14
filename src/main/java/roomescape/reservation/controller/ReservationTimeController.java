package roomescape.reservation.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.reservation.dto.ReservationTimeResponse;
import roomescape.reservation.service.ReservationTimeService;

import java.util.List;

@RestController
public class ReservationTimeController {

    private final ReservationTimeService reservationTimeService;

    public ReservationTimeController(final ReservationTimeService reservationTimeService) {
        this.reservationTimeService = reservationTimeService;
    }

    @GetMapping("/times")
    public List<ReservationTimeResponse> getReservationTimes() {
        return reservationTimeService.getReservationTimes()
                .stream()
                .map(ReservationTimeResponse::from)
                .toList();
    }
}
