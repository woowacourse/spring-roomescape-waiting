package roomescape.reservation.controller;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import roomescape.global.auth.annotation.RequireRole;
import roomescape.member.domain.MemberRole;
import roomescape.reservation.dto.response.ReservationResponse;
import roomescape.reservation.service.WaitingService;

@RestController
public class WaitingController {

    private final WaitingService waitingService;

    public WaitingController(final WaitingService waitingService) {
        this.waitingService = waitingService;
    }

    @GetMapping("/waiting")
    public ResponseEntity<List<ReservationResponse>> findWaitings(
    ) {
        return ResponseEntity.ok(waitingService.findWaitings());
    }

    @RequireRole(MemberRole.USER)
    @DeleteMapping("/waiting/{id}")
    public ResponseEntity<Void> deleteReservations(
            @PathVariable("id") Long id
    ) {
        waitingService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
