package roomescape.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import roomescape.auth.LoginMember;
import roomescape.controller.request.CreateReservationRequest;
import roomescape.controller.request.LoginMemberInfo;
import roomescape.controller.response.MemberReservationResponse;
import roomescape.controller.response.ReservationResponse;
import roomescape.service.ReservationService;
import roomescape.service.result.ReservationResult;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
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
            @RequestBody CreateReservationRequest createReservationRequest,
            @LoginMember LoginMemberInfo loginMemberInfo) {

        ReservationResult reservationResult = reservationService.create(createReservationRequest.toServiceParam(loginMemberInfo.id()), LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.CREATED).body(ReservationResponse.from(reservationResult));
    }

    @DeleteMapping("/{reservationId}")
    public ResponseEntity<Void> deleteReservation(@PathVariable("reservationId") Long reservationId) {
        reservationService.deleteById(reservationId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/mine")
    public ResponseEntity<List<MemberReservationResponse>> getMyReservations(@LoginMember LoginMemberInfo loginMemberInfo) {
        List<ReservationResult> reservationResults = reservationService.findMemberReservationsById(loginMemberInfo.id());
        return ResponseEntity.ok(MemberReservationResponse.from(reservationResults));
    }
}
