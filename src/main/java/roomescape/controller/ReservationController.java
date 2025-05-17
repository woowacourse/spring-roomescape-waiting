package roomescape.controller;

import java.time.LocalDate;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.LoginMember;
import roomescape.controller.request.CreateReservationRequest;
import roomescape.controller.request.LoginMemberInfo;
import roomescape.controller.response.MemberReservationResponse;
import roomescape.controller.response.ReservationResponse;
import roomescape.service.ReservationCreationService;
import roomescape.service.ReservationService;
import roomescape.service.result.ReservationResult;
import roomescape.service.result.ReservationWithWaitingResult;

@RestController
@RequestMapping("/reservations")
public class ReservationController {

    private final ReservationCreationService reservationCreationService;
    private final ReservationService reservationService;

    public ReservationController(ReservationCreationService reservationCreationService, ReservationService reservationService) {
        this.reservationCreationService = reservationCreationService;
        this.reservationService = reservationService;
    }

    @GetMapping
    public ResponseEntity<List<ReservationResponse>> findReservations(@RequestParam(required = false) Long memberId,
                                                                      @RequestParam(required = false) Long themeId,
                                                                      @RequestParam(required = false) LocalDate dateFrom,
                                                                      @RequestParam(required = false) LocalDate dateTo) {
        List<ReservationResult> reservationResults = reservationService.getReservationsInConditions(memberId, themeId, dateFrom, dateTo);
        List<ReservationResponse> reservationResponses = reservationResults.stream()
                .map(ReservationResponse::from)
                .toList();
        return ResponseEntity.ok(reservationResponses);
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> createReservation(
            @RequestBody CreateReservationRequest createReservationRequest,
            @LoginMember LoginMemberInfo loginMemberInfo) {

        ReservationResult reservationResult = reservationCreationService.create(createReservationRequest.toServiceParam(loginMemberInfo.id()));
        return ResponseEntity.status(HttpStatus.CREATED).body(ReservationResponse.from(reservationResult));
    }

    @PostMapping("/waitings")
    public ResponseEntity<ReservationResponse> createWaitingReservation(
            @RequestBody CreateReservationRequest createReservationRequest,
            @LoginMember LoginMemberInfo loginMemberInfo) {

        ReservationResult reservationResult = reservationCreationService.createWaiting(createReservationRequest.toServiceParam(loginMemberInfo.id()));
        return ResponseEntity.status(HttpStatus.CREATED).body(ReservationResponse.from(reservationResult));
    }

    @DeleteMapping("/waitings/{reservationId}")
    public ResponseEntity<Void> deleteWaitingReservation(
            @PathVariable("reservationId") Long reservationId,
            @LoginMember LoginMemberInfo loginMemberInfo) {

        reservationService.deleteWaitingById(reservationId, loginMemberInfo);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{reservationId}")
    public ResponseEntity<Void> deleteReservation(@PathVariable("reservationId") Long reservationId) {
        reservationService.deleteById(reservationId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/mine")
    public ResponseEntity<List<MemberReservationResponse>> getMyReservations(@LoginMember LoginMemberInfo loginMemberInfo) {
        List<ReservationWithWaitingResult> results = reservationService.getMemberReservationsById(
                loginMemberInfo.id());

        return ResponseEntity.ok(MemberReservationResponse.from(results));
    }
}
