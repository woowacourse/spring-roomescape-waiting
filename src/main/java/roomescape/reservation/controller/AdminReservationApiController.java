package roomescape.reservation.controller;

import jakarta.validation.Valid;
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
import roomescape.member.controller.response.MemberResponse;
import roomescape.member.resolver.LoginMember;
import roomescape.reservation.controller.request.ReservationCreateRequest;
import roomescape.reservation.controller.request.ReservationRequest;
import roomescape.reservation.controller.response.MemberReservationResponse;
import roomescape.reservation.controller.response.ReservationResponse;
import roomescape.reservation.service.ReservationWaitingService;

@RestController
@RequestMapping("/admin")
public class AdminReservationApiController {

    private final ReservationWaitingService reservationWaitingService;

    public AdminReservationApiController(ReservationWaitingService reservationWaitingService) {
        this.reservationWaitingService = reservationWaitingService;
    }

    @GetMapping("/reservations")
    public ResponseEntity<List<ReservationResponse>> searchReservations(
            @RequestParam(required = false) Long memberId,
            @RequestParam(required = false) Long themeId,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate
    ) {
        List<ReservationResponse> reservationResponses = reservationWaitingService.findAllReservationsByFilter(memberId, themeId,
                startDate,
                endDate);
        return ResponseEntity.ok(reservationResponses);
    }

    @PostMapping("/reservations")
    public ResponseEntity<ReservationResponse> createReservation(@RequestBody @Valid ReservationCreateRequest request) {
        ReservationResponse response = reservationWaitingService.createReservation(request.memberId(), new ReservationRequest(
                request.date(), request.themeId(), request.timeId()
        ));
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @DeleteMapping("/reservations/{id}")
    public ResponseEntity<Void> deleteReservation(@PathVariable Long id) {
        reservationWaitingService.deleteReservationAndUpdateWaiting(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/waitings")
    public ResponseEntity<List<MemberReservationResponse>> getMemberReservations(
            @LoginMember MemberResponse memberResponse) {
        List<MemberReservationResponse> waitings = reservationWaitingService.findWaitingsWithRankByMemberId(memberResponse.id());
        return ResponseEntity.ok().body(waitings);
    }
}
