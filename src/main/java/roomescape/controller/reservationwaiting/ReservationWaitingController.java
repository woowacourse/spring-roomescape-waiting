package roomescape.controller.reservationwaiting;

import java.net.URI;
import org.springframework.http.ResponseEntity;
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
    @PostMapping("/waitings")
    public ResponseEntity<Void> saveReservationWaiting(@RequestBody ReservationWaitingRequest request,
                                                       @LoginMember Member member) {
        Long id = reservationWaitingService.saveReservationWaiting(request, member);
        return ResponseEntity.created(URI.create("/waitings/" + id)).build();
    }
}
