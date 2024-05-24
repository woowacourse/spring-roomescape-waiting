package roomescape.controller.api;

import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.AuthenticatedMember;
import roomescape.domain.reservationwait.ReservationWait;
import roomescape.domain.member.Member;
import roomescape.service.dto.request.ReservationWaitSaveRequest;
import roomescape.service.dto.response.reservationwait.ReservationWaitResponse;
import roomescape.service.reservationwait.ReservationWaitCreateService;
import roomescape.service.reservationwait.ReservationWaitDeleteService;

@RestController
public class ReservationWaitApiController {

    private final ReservationWaitCreateService reservationWaitCreateService;
    private final ReservationWaitDeleteService reservationWaitDeleteService;

    public ReservationWaitApiController(ReservationWaitCreateService reservationWaitCreateService,
                                        ReservationWaitDeleteService reservationWaitDeleteService) {
        this.reservationWaitCreateService = reservationWaitCreateService;
        this.reservationWaitDeleteService = reservationWaitDeleteService;
    }

    @PostMapping("/reservations/wait")
    public ResponseEntity<ReservationWaitResponse> addReservationWait(
            @RequestBody @Valid ReservationWaitSaveRequest request,
            @AuthenticatedMember Member member) {
        ReservationWait newReservationWait = reservationWaitCreateService.create(request, member);
        return ResponseEntity.created(URI.create("/reservations/wait/" + newReservationWait.getId()))
                .body(new ReservationWaitResponse(newReservationWait));
    }

    @PutMapping("/reservations/wait/{reservationWaitId}")
    public ResponseEntity<Void> cancelReservationWait(@PathVariable long reservationWaitId) {
        reservationWaitDeleteService.cancelById(reservationWaitId);
        return ResponseEntity.noContent().build();
    }
}
