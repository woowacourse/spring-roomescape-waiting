package roomescape.reservationwaiting.controller;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.LoginMember;
import roomescape.member.domain.Member;
import roomescape.reservationwaiting.dto.ReservationWaitingRequest;
import roomescape.reservationwaiting.dto.ReservationWaitingResponse;
import roomescape.reservationwaiting.dto.ReservationWaitingTurnResponse;
import roomescape.reservationwaiting.service.ReservationWaitingService;

@RestController
@RequestMapping("/waitings")
public class ReservationWaitingController {

    private final ReservationWaitingService reservationWaitingService;

    public ReservationWaitingController(ReservationWaitingService reservationWaitingService) {
        this.reservationWaitingService = reservationWaitingService;
    }

    @PostMapping
    public ResponseEntity<ReservationWaitingResponse> createWaiting(@LoginMember Member member,
                                                                    @Valid @RequestBody ReservationWaitingRequest request) {
        ReservationWaitingResponse response = reservationWaitingService.createWaiting(member, request);
        return ResponseEntity.created(URI.create("/waitings/" + response.id())).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ReservationWaitingTurnResponse>> getMyWaitings(@LoginMember Member member) {
        return ResponseEntity.ok(reservationWaitingService.getWaitingByMemberId(member.getId()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWaiting(@LoginMember Member member, @PathVariable Long id) {
        reservationWaitingService.deleteWaiting(id, member.getId());
        return ResponseEntity.noContent().build();
    }
}