package roomescape.reservationwait;

import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.LoginMember;
import roomescape.reservationwait.dto.ReservationWaitResponse;

@RequestMapping("/api/v1/reservations/{reservationId}/waits")
@RestController
public class ReservationWaitController {

    private final ReservationWaitService reservationWaitService;

    public ReservationWaitController(ReservationWaitService reservationWaitService) {
        this.reservationWaitService = reservationWaitService;
    }

    @PostMapping
    public ResponseEntity<ReservationWaitResponse> createReservationWait(
            @PathVariable Long reservationId,
            @LoginMember Long memberId) {
        return ResponseEntity.created(URI.create("/api/v1/reservations/" + reservationId + "/waits"))
                .body(ReservationWaitResponse.from(
                        reservationWaitService.createReservationWait(memberId, reservationId)));
    }

    @DeleteMapping("/mine")
    public ResponseEntity<Void> deleteReservationWait(
            @PathVariable Long reservationId,
            @LoginMember Long memberId) {
        reservationWaitService.deleteReservationWait(reservationId, memberId);
        return ResponseEntity.noContent().build();
    }
}
