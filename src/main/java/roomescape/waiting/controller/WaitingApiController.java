package roomescape.waiting.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.member.controller.response.MemberResponse;
import roomescape.member.resolver.LoginMember;
import roomescape.reservation.controller.response.ReservationResponse;
import roomescape.user.controller.dto.request.ReservationRequest;
import roomescape.waiting.service.WaitingService;

@RestController
@RequestMapping("/reservations/waiting/")
public class WaitingApiController {

    private final WaitingService waitingService;

    public WaitingApiController(WaitingService waitingService) {
        this.waitingService = waitingService;
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> createWaitingReservation(
            @LoginMember MemberResponse memberResponse,
            @RequestBody ReservationRequest request
    ) {
        ReservationResponse response = waitingService.createById(memberResponse.id(), request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Void> deleteWaitingReservation(
            @PathVariable Long id
    ) {
        waitingService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
