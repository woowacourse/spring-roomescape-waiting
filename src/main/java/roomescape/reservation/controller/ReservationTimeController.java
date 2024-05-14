package roomescape.reservation.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import roomescape.global.auth.annotation.Admin;
import roomescape.global.dto.response.ApiResponse;
import roomescape.reservation.dto.request.ReservationTimeRequest;
import roomescape.reservation.dto.response.ReservationTimeResponse;
import roomescape.reservation.dto.response.ReservationTimesResponse;
import roomescape.reservation.service.ReservationTimeService;

@RestController
public class ReservationTimeController {
    private final ReservationTimeService reservationTimeService;

    public ReservationTimeController(final ReservationTimeService reservationTimeService) {
        this.reservationTimeService = reservationTimeService;
    }

    @Admin
    @GetMapping("/times")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<ReservationTimesResponse> getAllTimes() {

        return ApiResponse.success(reservationTimeService.findAllTimes());
    }

    @Admin
    @PostMapping("/times")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ReservationTimeResponse> saveTime(
            @RequestBody final ReservationTimeRequest reservationTimeRequest,
            final HttpServletResponse response
    ) {
        ReservationTimeResponse reservationTimeResponse = reservationTimeService.addTime(reservationTimeRequest);
        response.setHeader(HttpHeaders.LOCATION, "/times/" + reservationTimeResponse.id());

        return ApiResponse.success(reservationTimeResponse);
    }

    @Admin
    @DeleteMapping("/times/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ApiResponse<Void> removeTime(@PathVariable final Long id) {
        reservationTimeService.removeTimeById(id);

        return ApiResponse.success();
    }
}
