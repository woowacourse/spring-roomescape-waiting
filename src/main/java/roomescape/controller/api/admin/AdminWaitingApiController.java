package roomescape.controller.api.admin;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.AuthenticatedMember;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.service.dto.response.WaitingResponse;
import roomescape.service.reservation.ReservationFindService;

import java.util.List;

@RestController
public class AdminWaitingApiController {

    private final ReservationFindService reservationFindService;

    public AdminWaitingApiController(ReservationFindService reservationFindService) {
        this.reservationFindService = reservationFindService;
    }

    @GetMapping("/admin/waitings")
    public ResponseEntity<List<WaitingResponse>> getWaiting(@AuthenticatedMember Member member) {
        List<Reservation> waitings = reservationFindService.findWaitings();
        return ResponseEntity.ok(
                waitings.stream()
                        .map(WaitingResponse::new)
                        .toList()
        );
    }
}
