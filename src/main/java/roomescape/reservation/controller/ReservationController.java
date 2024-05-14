package roomescape.reservation.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import roomescape.global.auth.annotation.Admin;
import roomescape.global.auth.annotation.MemberId;
import roomescape.global.dto.response.ApiResponse;
import roomescape.reservation.dto.request.ReservationRequest;
import roomescape.reservation.dto.response.ReservationResponse;
import roomescape.reservation.dto.response.ReservationTimeInfosResponse;
import roomescape.reservation.dto.response.ReservationsResponse;
import roomescape.reservation.service.ReservationService;

import java.time.LocalDate;

@RestController
public class ReservationController {
    private final ReservationService reservationService;

    public ReservationController(final ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @Admin
    @GetMapping("/reservations")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<ReservationsResponse> getAllReservations() {
        return ApiResponse.success(reservationService.findAllReservations());
    }

    @GetMapping("/reservations/themes/{themeId}/times")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<ReservationTimeInfosResponse> getReservationTimeInfos(
            @PathVariable final Long themeId,
            @RequestParam final LocalDate date) {
        return ApiResponse.success(reservationService.findReservationsByDateAndThemeId(date, themeId));
    }

    @Admin
    @GetMapping("/reservations/search")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<ReservationsResponse> getReservationBySearching(
            @RequestParam(required = false) final Long themeId,
            @RequestParam(required = false) final Long memberId,
            @RequestParam(required = false) final LocalDate dateFrom,
            @RequestParam(required = false) final LocalDate dateTo
    ) {
        return ApiResponse.success(
                reservationService.findReservationsByThemeIdAndMemberIdBetweenDate(themeId, memberId, dateFrom, dateTo));
    }

    @PostMapping("/reservations")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ReservationResponse> saveReservation(
            @RequestBody final ReservationRequest reservationRequest,
            @MemberId final Long memberId,
            final HttpServletResponse response
    ) {
        ReservationResponse reservationResponse = reservationService.addReservation(reservationRequest, memberId);

        response.setHeader(HttpHeaders.LOCATION, "/reservations/" + reservationResponse.id());
        return ApiResponse.success(reservationResponse);
    }

    @DeleteMapping("/reservations/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ApiResponse<Void> removeReservation(@MemberId final Long memberId, @PathVariable("id") final Long reservationId) {
        reservationService.removeReservationById(reservationId, memberId);

        return ApiResponse.success();
    }
}
