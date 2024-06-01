package roomescape.reservation.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import roomescape.global.dto.response.ApiResponse;
import roomescape.reservation.dto.response.ReservationTimesResponse;
import roomescape.reservation.service.ReservationTimeService;

@RestController
public class ReservationTimeController {
    private final ReservationTimeService reservationTimeService;

    public ReservationTimeController(final ReservationTimeService reservationTimeService) {
        this.reservationTimeService = reservationTimeService;
    }

    @GetMapping("/times")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<ReservationTimesResponse> getAllTimes() {
        return ApiResponse.success(reservationTimeService.findAllTimes());
    }
}
