package roomescape.controller.api.admin;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.AuthenticatedMember;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.service.dto.response.WaitingResponse;
import roomescape.service.reservation.ReservationService;

import java.util.List;

@RestController
public class AdminWaitingApiController {

    private final ReservationService reservationService;

    public AdminWaitingApiController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping("/api/admin/waitings")
    public ResponseEntity<List<WaitingResponse>> getWaiting(@AuthenticatedMember Member member) {
        List<Reservation> waitings = reservationService.findWaitings();
        return ResponseEntity.ok(
                waitings.stream()
                        .map(WaitingResponse::new)
                        .toList()
        );
    }
}
