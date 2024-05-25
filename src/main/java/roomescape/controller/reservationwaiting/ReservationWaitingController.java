package roomescape.controller.reservationwaiting;

import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.controller.auth.LoginMember;
import roomescape.controller.auth.RoleAllowed;
import roomescape.domain.member.Member;
import roomescape.service.reservationwaiting.ReservationWaitingService;
import roomescape.service.reservationwaiting.dto.ReservationWaitingRequest;

@RestController
public class ReservationWaitingController {
    private final ReservationWaitingService reservationWaitingService;

    public ReservationWaitingController(ReservationWaitingService reservationWaitingService) {
        this.reservationWaitingService = reservationWaitingService;
    }

    @RoleAllowed
    @PostMapping("/reservations/waitings")
    public ResponseEntity<Void> saveReservationWaiting(@RequestBody ReservationWaitingRequest request,
                                                       @LoginMember Member member) {
        Long id = reservationWaitingService.saveReservationWaiting(request, member);
        return ResponseEntity.created(URI.create("/reservations/waitings/" + id)).build();
    }

    @RoleAllowed
    @DeleteMapping("/reservations/waitings/{reservationId}")
    public ResponseEntity<Void> deleteReservation(@PathVariable Long reservationId,
                                                  @LoginMember Member member) {
        reservationWaitingService.deleteReservationWaiting(reservationId, member);
        return ResponseEntity.noContent().build();
    }
}
