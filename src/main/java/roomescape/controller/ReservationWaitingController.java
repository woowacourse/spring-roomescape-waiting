package roomescape.controller;

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

import roomescape.config.Authorization;
import roomescape.dto.ReservationRequest;
import roomescape.dto.ReservationWaitingResponse;
import roomescape.service.ReservationWaitingService;

@RequestMapping("/reservations/waitings")
@RestController
public class ReservationWaitingController {

    private final ReservationWaitingService reservationWaitingService;

    public ReservationWaitingController(ReservationWaitingService reservationWaitingService) {
        this.reservationWaitingService = reservationWaitingService;
    }

    @PostMapping
    public ResponseEntity<ReservationWaitingResponse> createWaiting(
            @Authorization long memberId,
            @RequestBody ReservationRequest request
    ) {
        ReservationRequest requestWithMemberId = new ReservationRequest(memberId, request.date(), request.timeId(),
                request.themeId());
        ReservationWaitingResponse response = reservationWaitingService.createWaiting(requestWithMemberId);
        URI location = URI.create("/reservations/waitings/" + response.id());
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ReservationWaitingResponse>> findWaitings() {
        List<ReservationWaitingResponse> waitings = reservationWaitingService.findWaitings();
        return ResponseEntity.ok(waitings);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        reservationWaitingService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
