package roomescape.reservation.presentation.controller.admin;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.common.security.annotation.RequireRole;
import roomescape.member.domain.MemberRole;
import roomescape.reservation.application.WaitingReservationApplicationService;
import roomescape.reservation.presentation.dto.response.WaitingWebResponse;

@RestController
@RequestMapping("/admin/waiting-reservations")
public class AdminWaitingReservationController {

    private final WaitingReservationApplicationService waitingReservationApplicationService;

    public AdminWaitingReservationController(
            final WaitingReservationApplicationService waitingReservationApplicationService) {
        this.waitingReservationApplicationService = waitingReservationApplicationService;
    }

    @RequireRole(MemberRole.ADMIN)
    @GetMapping
    public ResponseEntity<List<WaitingWebResponse>> findAll(
    ) {
        List<WaitingWebResponse> responses = waitingReservationApplicationService.findAll();
        return ResponseEntity.ok(responses);
    }

    @RequireRole(MemberRole.ADMIN)
    @DeleteMapping("/{waitingId}")
    public ResponseEntity<Void> cancel(
            @PathVariable Long waitingId
    ) {
        waitingReservationApplicationService.cancel(waitingId);
        return ResponseEntity.noContent().build();
    }
}
