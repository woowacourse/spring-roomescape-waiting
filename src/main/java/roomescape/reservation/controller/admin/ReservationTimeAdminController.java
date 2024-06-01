package roomescape.reservation.controller.admin;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import roomescape.global.auth.annotation.Admin;
import roomescape.global.dto.response.ApiResponse;
import roomescape.reservation.dto.request.ReservationTimeRequest;
import roomescape.reservation.dto.response.ReservationTimeResponse;
import roomescape.reservation.service.ReservationTimeService;

@RestController
public class ReservationTimeAdminController {

    private final ReservationTimeService reservationTimeService;

    public ReservationTimeAdminController(final ReservationTimeService reservationTimeService) {
        this.reservationTimeService = reservationTimeService;
    }

    @Admin
    @PostMapping("/admin/times")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ReservationTimeResponse> saveTime(
            @Valid @RequestBody final ReservationTimeRequest reservationTimeRequest,
            final HttpServletResponse response
    ) {
        ReservationTimeResponse reservationTimeResponse = reservationTimeService.addTime(reservationTimeRequest);
        response.setHeader(HttpHeaders.LOCATION, "/times/" + reservationTimeResponse.id());

        return ApiResponse.success(reservationTimeResponse);
    }

    @Admin
    @DeleteMapping("/admin/times/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ApiResponse<Void> removeTime(
            @NotNull(message = "timeId는 null 일 수 없습니다.") @PathVariable final Long id
    ) {
        reservationTimeService.removeTimeById(id);

        return ApiResponse.success();
    }
}
