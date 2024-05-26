package roomescape.reservation.controller;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import roomescape.global.auth.annotation.Admin;
import roomescape.global.auth.annotation.Auth;
import roomescape.global.auth.annotation.MemberId;
import roomescape.global.dto.response.ApiResponse;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.dto.request.ReservationRequest;
import roomescape.reservation.dto.response.MemberReservationsResponse;
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
    public ApiResponse<ReservationsResponse> getReservationsByStatus(@RequestParam final ReservationStatus status) {
        return ApiResponse.success(reservationService.findReservationsByStatus(status));
    }

    @Auth
    @GetMapping("/reservations/my")
    public ApiResponse<MemberReservationsResponse> getMemberReservations(@MemberId final Long memberId) {
        return ApiResponse.success(reservationService.findUnexpiredReservationByMemberId(memberId));
    }

    @GetMapping("/reservations/themes/{themeId}/times")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<ReservationTimeInfosResponse> getReservationTimeInfos(
            @NotNull(message = "themeId는 null 일 수 없습니다.") @PathVariable final Long themeId,
            @NotNull(message = "날짜는 null일 수 없습니다.") @RequestParam final LocalDate date) {
        return ApiResponse.success(reservationService.findReservationsByDateAndThemeId(date, themeId));

    }

//    @Admin
//    @GetMapping("/reservations/search")
//    @ResponseStatus(HttpStatus.OK)
//    public ApiResponse<ReservationsResponse> getReservationBySearching(
//            @RequestParam(required = false) final Long themeId,
//            @RequestParam(required = false) final Long memberId,
//            @RequestParam(required = false) final LocalDate dateFrom,
//            @RequestParam(required = false) final LocalDate dateTo
//    ) {
//        return ApiResponse.success(
//                reservationService.searchWith(themeId, memberId, dateFrom, dateTo));
//    }

    // TODO: @Auth, @Admin 애노테이션 대신 @RequiredRole(value = ), @RequiredRole(values = {}) 애노테이션으로 변경
    @Auth
    @PostMapping("/reservations")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ReservationResponse> requestReservation(
            @Valid @RequestBody final ReservationRequest reservationRequest,
            @MemberId final Long memberId,
            final HttpServletResponse response
    ) {
        ReservationResponse reservationResponse = reservationService.addMemberReservation(
                reservationRequest, memberId, ReservationStatus.RESERVED);

        response.setHeader(HttpHeaders.LOCATION, "/reservations/" + reservationResponse.id());
        return ApiResponse.success(reservationResponse);
    }

    @Auth
    @PostMapping("/reservations/waitings")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ReservationResponse> requestReservationWaiting(
            @Valid @RequestBody final ReservationRequest reservationRequest,
            @MemberId final Long memberId,
            final HttpServletResponse response
    ) {
        ReservationResponse reservationResponse = reservationService.addMemberReservation(
                reservationRequest, memberId, ReservationStatus.WAITING);

        response.setHeader(HttpHeaders.LOCATION, "/reservations/waitings/" + reservationResponse.id());
        return ApiResponse.success(reservationResponse);
    }

    @Auth
    @DeleteMapping("/reservations/{memberReservationId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ApiResponse<Void> removeReservation(
            @MemberId final Long memberId,
            @NotNull(message = "reservationId는 null 일 수 없습니다.") @PathVariable("memberReservationId") final Long memberReservationId
    ) {
        reservationService.removeMemberReservationById(memberReservationId, memberId);

        return ApiResponse.success();
    }

    @Auth
    @DeleteMapping("/reservations/waitings/{memberReservationId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ApiResponse<Void> removeWaitingReservation(
            @MemberId final Long memberId,
            @NotNull(message = "memberReservationId는 null 일 수 없습니다.") @PathVariable("memberReservationId") final Long memberReservationId
    ) {
        reservationService.removeWaitingReservationById(memberReservationId, memberId);

        return ApiResponse.success();
    }

    @Admin
    @PatchMapping("/reservations/waitings/{memberReservationId}")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<Void> approveWaitingReservation(
            @NotNull(message = "reservationId는 null 일 수 없습니다.") @PathVariable("memberReservationId") final Long memberReservationId
    ) {
        reservationService.approveWaitingReservation(memberReservationId);

        return ApiResponse.success();
    }

    @Admin
    @GetMapping("/reservations/waitings")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<ReservationsResponse> getFirstOrderWaitingReservations() {
        return ApiResponse.success(reservationService.findFirstOrderWaitingReservations());
    }
}
