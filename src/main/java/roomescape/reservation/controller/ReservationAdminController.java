package roomescape.reservation.controller;

import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import roomescape.global.auth.annotation.Admin;
import roomescape.global.dto.response.ApiResponse;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.dto.response.ReservationsResponse;
import roomescape.reservation.service.ReservationService;

import java.time.LocalDate;

@RestController
public class ReservationAdminController {

    private final ReservationService reservationService;

    public ReservationAdminController(final ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @Admin
    @GetMapping("/admin/reservations")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<ReservationsResponse> getReservationsByStatus(@RequestParam final ReservationStatus status) {
        return ApiResponse.success(reservationService.findReservationsByStatus(status));
    }

    @Admin
    @GetMapping("/admin/reservations/search")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<ReservationsResponse> getReservationBySearching(
            @RequestParam(required = false) final Long themeId,
            @RequestParam(required = false) final Long memberId,
            @RequestParam(required = false) final LocalDate dateFrom,
            @RequestParam(required = false) final LocalDate dateTo
    ) {
        return ApiResponse.success(
                reservationService.searchWith(themeId, memberId, dateFrom, dateTo));
    }

    @Admin
    @PatchMapping("/admin/reservations/waitings/{memberReservationId}")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<Void> approveWaitingReservation(
            @NotNull(message = "memberReservationId는 null 일 수 없습니다.") @PathVariable("memberReservationId") final Long memberReservationId
    ) {
        reservationService.approveWaitingReservation(memberReservationId);

        return ApiResponse.success();
    }

    @Admin
    @GetMapping("/admin/reservations/waitings")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<ReservationsResponse> getFirstOrderWaitingReservations() {
        return ApiResponse.success(reservationService.findFirstOrderWaitingReservations());
    }
}
