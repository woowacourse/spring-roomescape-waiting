package roomescape.reservationwaiting.controller;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.LoginMember;
import roomescape.member.domain.Member;
import roomescape.reservationwaiting.dto.ReservationWaitingTurnResponse;
import roomescape.reservationwaiting.service.ReservationWaitingService;
import roomescape.reservationwaiting.service.WaitingWithTurn;

@RestController
@RequestMapping("/waitings")
public class ReservationWaitingController {

    private final ReservationWaitingService reservationWaitingService;

    public ReservationWaitingController(ReservationWaitingService reservationWaitingService) {
        this.reservationWaitingService = reservationWaitingService;
    }

    @GetMapping
    public ResponseEntity<List<ReservationWaitingTurnResponse>> getMyWaitings(@LoginMember Member member) {
        List<ReservationWaitingTurnResponse> responses = reservationWaitingService.getWaitingByMemberId(member.getId())
                .stream()
                .map(it -> ReservationWaitingTurnResponse.from(it.waiting(), it.turn()))
                .toList();
        return ResponseEntity.ok(responses);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWaiting(@LoginMember Member member, @PathVariable Long id) {
        reservationWaitingService.deleteWaiting(id, member.getId());
        return ResponseEntity.noContent().build();
    }
}
