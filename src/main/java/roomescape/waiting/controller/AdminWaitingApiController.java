package roomescape.waiting.controller;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.reservation.controller.response.ReservationResponse;
import roomescape.user.controller.dto.response.MemberReservationResponse;
import roomescape.waiting.service.WaitingService;

@RestController
@RequestMapping("/admin/waitings")
public class AdminWaitingApiController {

    private final WaitingService waitingService;

    public AdminWaitingApiController(WaitingService waitingService) {
        this.waitingService = waitingService;
    }

    @GetMapping
    public ResponseEntity<List<MemberReservationResponse>> getWaitings() {
        List<MemberReservationResponse> reservationResponses = waitingService.findAllWithRank();
        return ResponseEntity.ok().body(reservationResponses);
    }

    @PostMapping("{id}")
    public ResponseEntity<ReservationResponse> approveWaiting(
            @PathVariable Long id
    ) {
        ReservationResponse response = waitingService.approveWaitingById(id);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Void> rejectWaiting(
            @PathVariable Long id
    ) {
        waitingService.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
