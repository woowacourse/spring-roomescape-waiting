package roomescape.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import roomescape.auth.LoginMember;
import roomescape.controller.request.CreateReservationRequest;
import roomescape.controller.request.LoginMemberInfo;
import roomescape.controller.response.MyReservationResponse;
import roomescape.controller.response.ReservationResponse;
import roomescape.domain.ReservationStatus;
import roomescape.service.ReservationService;
import roomescape.service.result.ReservationResult;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping("/reservations")
    public ResponseEntity<List<ReservationResponse>> findReservations(@RequestParam(required = false) Long memberId,
                                                                      @RequestParam(required = false) Long themeId,
                                                                      @RequestParam(required = false) LocalDate dateFrom,
                                                                      @RequestParam(required = false) LocalDate dateTo,
                                                                      @RequestParam(required = false) ReservationStatus status
                                                                      ) {
        List<ReservationResult> reservationResults = reservationService.findReservationsInConditions(memberId, themeId, dateFrom, dateTo, status);
        List<ReservationResponse> reservationResponses = reservationResults.stream()
                .map(ReservationResponse::from)
                .toList();
        return ResponseEntity.ok(reservationResponses);
    }

    @GetMapping("/reservations-mine")
    public ResponseEntity<List<MyReservationResponse>> findMyReservations(@LoginMember LoginMemberInfo loginMemberInfo) {
        List<ReservationResult> reservationResults = reservationService.findByMemberId(loginMemberInfo.id());
        List<MyReservationResponse> myReservationResponses = reservationResults.stream()
                .map(MyReservationResponse::from)
                .toList();
        return ResponseEntity.ok(myReservationResponses);
    }

    @PostMapping("/reservations")
    public ResponseEntity<ReservationResponse> createReservation(
            @RequestBody CreateReservationRequest createReservationRequest,
            @LoginMember LoginMemberInfo loginMemberInfo) {
        Long reservationId = reservationService.create(createReservationRequest.toServiceParam(loginMemberInfo.id()), LocalDateTime.now());
        ReservationResult reservationResult = reservationService.findById(reservationId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ReservationResponse.from(reservationResult));
    }

    @PatchMapping("/reservations/{reservationId}/approve")
    public ResponseEntity<ReservationResponse> approveWaitingReservation(@PathVariable Long reservationId) {
        reservationService.approveWaitingReservation(reservationId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/reservations/{reservationId}")
    public ResponseEntity<Void> deleteReservation(@PathVariable("reservationId") Long reservationId) {
        reservationService.deleteById(reservationId);
        return ResponseEntity.noContent().build();
    }
}
