package roomescape.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import roomescape.auth.LoginMember;
import roomescape.controller.request.CreateReservationRequest;
import roomescape.controller.request.LoginMemberInfo;
import roomescape.controller.response.MemberReservationResponse;
import roomescape.controller.response.ReservationResponse;
import roomescape.service.ReservationService;
import roomescape.service.WaitingService;
import roomescape.service.result.ReservationResult;
import roomescape.service.result.WaitingWithRankResult;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/reservations")
public class ReservationController {

    private final ReservationService reservationService;
    private final WaitingService waitingService;

    public ReservationController(ReservationService reservationService, final WaitingService waitingService) {
        this.reservationService = reservationService;
        this.waitingService = waitingService;
    }

    @GetMapping
    public ResponseEntity<List<ReservationResponse>> findReservations(@RequestParam(required = false) Long memberId,
                                                                      @RequestParam(required = false) Long themeId,
                                                                      @RequestParam(required = false) LocalDate dateFrom,
                                                                      @RequestParam(required = false) LocalDate dateTo) {
        List<ReservationResult> reservationResults = reservationService.findReservationsInConditions(memberId, themeId, dateFrom, dateTo);
        List<ReservationResponse> reservationResponses = reservationResults.stream()
                .map(ReservationResponse::from)
                .toList();
        return ResponseEntity.ok(reservationResponses);
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> createReservation(
            @Valid @RequestBody CreateReservationRequest createReservationRequest,
            @LoginMember LoginMemberInfo loginMemberInfo) {

        ReservationResult reservationResult = reservationService.create(createReservationRequest.toServiceParam(loginMemberInfo.id()), LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.CREATED).body(ReservationResponse.from(reservationResult));
    }

    @DeleteMapping("/{reservationId}")
    public ResponseEntity<Void> deleteReservation(@PathVariable("reservationId") Long reservationId) {
        reservationService.deleteByIdAndApproveFirstWaiting(reservationId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/member")
    public ResponseEntity<List<MemberReservationResponse>> getMemberReservations(@LoginMember LoginMemberInfo loginMemberInfo) {
        List<ReservationResult> reservationResults = reservationService.findReservationsByMemberId(loginMemberInfo.id());
        List<WaitingWithRankResult> waitingsWithRank = waitingService.findWaitingsWithRankByMemberId(loginMemberInfo.id());
        return ResponseEntity.ok(MemberReservationResponse.from(reservationResults, waitingsWithRank));
    }
}
