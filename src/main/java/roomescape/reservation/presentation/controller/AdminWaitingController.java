package roomescape.reservation.presentation.controller;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.global.auth.Auth;
import roomescape.member.domain.Role;
import roomescape.reservation.application.service.WaitingService;
import roomescape.reservation.presentation.dto.WaitingResponse;

@RestController
@RequestMapping("/admin/reservations/waiting")
public class AdminWaitingController {

    private final WaitingService waitingService;

    public AdminWaitingController(final WaitingService waitingService) {
        this.waitingService = waitingService;
    }

    @Auth(Role.ADMIN)
    @GetMapping
    public ResponseEntity<List<WaitingResponse>> getWaitings(
    ) {
        return ResponseEntity.ok().body(
                waitingService.getWaitings()
        );
    }
}
