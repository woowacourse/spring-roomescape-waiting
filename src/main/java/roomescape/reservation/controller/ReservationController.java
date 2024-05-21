package roomescape.reservation.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import roomescape.auth.domain.AuthInfo;
import roomescape.global.annotation.LoginUser;
import roomescape.reservation.controller.dto.*;
import roomescape.reservation.service.ReservationService;
import roomescape.reservation.service.WaitingReservationService;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

@RestController
public class ReservationController {

    private final ReservationService reservationService;
    private final WaitingReservationService waitingReservationService;

    public ReservationController(ReservationService reservationService, WaitingReservationService waitingReservationService) {
        this.reservationService = reservationService;
        this.waitingReservationService = waitingReservationService;
    }

    @GetMapping("/reservations")
    public List<ReservationResponse> reservations(
            @RequestParam(value = "themeId", required = false) Long themeId,
            @RequestParam(value = "memberId", required = false) Long memberId,
            @RequestParam(value = "dateFrom", required = false) LocalDate startDate,
            @RequestParam(value = "dateTo", required = false) LocalDate endDate
    ) {
        return reservationService.findMemberReservations(
                new ReservationQueryRequest(themeId, memberId, startDate, endDate));
    }

    @PostMapping("/reservations")
    public ResponseEntity<ReservationResponse> create(@LoginUser AuthInfo authInfo,
                                                      @RequestBody @Valid ReservationRequest reservationRequest) {
        ReservationResponse response = reservationService.createMemberReservation(authInfo, reservationRequest);
        return ResponseEntity.created(URI.create("/reservations/" + response.memberReservationId())).body(response);
    }

    @DeleteMapping("/reservations/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@LoginUser AuthInfo authInfo,
                                       @PathVariable("id") @Min(1) long reservationMemberId) {
        reservationService.deleteMemberReservation(authInfo, reservationMemberId);
    }

    @GetMapping("/reservations/mine")
    public List<MyReservationResponse> getMyReservations(@LoginUser AuthInfo authInfo) {
        List<MyReservationWithStatus> myReservationWithStatuses = reservationService.findMyReservations(authInfo);
        return waitingReservationService.handleWaitingOrder(myReservationWithStatuses);
    }
}
