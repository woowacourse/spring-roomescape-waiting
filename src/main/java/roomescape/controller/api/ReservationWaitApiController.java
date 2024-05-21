package roomescape.controller.api;

import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.AuthenticatedMember;
import roomescape.domain.ReservationWait;
import roomescape.domain.member.Member;
import roomescape.service.dto.request.ReservationWaitSaveRequest;
import roomescape.service.dto.response.reservationwait.ReservationWaitResponse;
import roomescape.service.reservationwait.ReservationWaitCreateService;

@RestController
public class ReservationWaitApiController {

    private ReservationWaitCreateService reservationWaitCreateService;

    public ReservationWaitApiController(ReservationWaitCreateService reservationWaitCreateService) {
        this.reservationWaitCreateService = reservationWaitCreateService;
    }

    @PostMapping("/reservations/wait")
    public ResponseEntity<ReservationWaitResponse> addReservationWait(
            @RequestBody @Valid ReservationWaitSaveRequest request,
            @AuthenticatedMember Member member) {
        ReservationWait newReservationWait = reservationWaitCreateService.create(request, member);
        return ResponseEntity.created(URI.create("/reservations/wait/" + newReservationWait.getId()))
                .body(new ReservationWaitResponse(newReservationWait));
    }
}
