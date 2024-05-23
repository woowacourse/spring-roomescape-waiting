package roomescape.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import roomescape.auth.LoginMemberId;
import roomescape.service.reservation.WaitingService;
import roomescape.service.reservation.dto.ReservationResponse;

import java.util.List;

@RestController
@RequestMapping("/waitings")
public class WaitingController {
    private final WaitingService waitingService;

    public WaitingController(WaitingService waitingService) {
        this.waitingService = waitingService;
    }

    @GetMapping
    public List<ReservationResponse> findAll() {
        return waitingService.findAll();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWaiting(@PathVariable("id") long reservationId, @LoginMemberId long memberId) {
        waitingService.deleteWaitingById(reservationId, memberId);
        return ResponseEntity.noContent().build();
    }
}
