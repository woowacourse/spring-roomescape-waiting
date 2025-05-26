package roomescape.reservation.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.global.auth.annotation.RoleRequired;
import roomescape.member.entity.RoleType;
import roomescape.reservation.dto.response.WaitingReadResponse;
import roomescape.reservation.service.WaitingService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/waitings")
public class AdminWaitingController {

    private final WaitingService waitingService;

    @PostMapping("/{id}/approve")
    @RoleRequired(roleType = RoleType.ADMIN)
    public ResponseEntity<Void> approveWaitingByAdmin(
            @PathVariable("id") Long id
    ) {
        waitingService.approveWaiting(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    @RoleRequired(roleType = RoleType.ADMIN)
    public ResponseEntity<List<WaitingReadResponse>> getAllWaitings() {
        List<WaitingReadResponse> responses = waitingService.getAllWaitings();
        return ResponseEntity.ok().body(responses);
    }

    @DeleteMapping("/{id}/reject")
    @RoleRequired(roleType = RoleType.ADMIN)
    public ResponseEntity<Void> rejectWaitingByAdmin(
            @PathVariable("id") Long id
    ) {
        waitingService.deleteWaitingByAdmin(id);
        return ResponseEntity.noContent().build();
    }
}
