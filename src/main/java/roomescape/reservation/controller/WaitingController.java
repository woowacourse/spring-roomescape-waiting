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
import roomescape.reservation.service.WaitingModuleService;

@RestController
public class WaitingController {

    private final WaitingModuleService waitingModuleService;

    public WaitingController(final WaitingModuleService waitingModuleService) {
        this.waitingModuleService = waitingModuleService;
    }

    @GetMapping("/waiting")
    public ResponseEntity<List<ReservationResponse>> findWaitings(
    ) {
        return ResponseEntity.ok(waitingModuleService.findWaitings());
    }

    @RequireRole(MemberRole.USER)
    @DeleteMapping("/waiting/{id}")
    public ResponseEntity<Void> deleteReservations(
            @PathVariable("id") Long id
    ) {
        waitingModuleService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
