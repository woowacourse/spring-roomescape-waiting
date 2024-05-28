package roomescape.controller;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
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
    public ResponseEntity<ReservationWaitingResponse> create(
            @Authorization long memberId,
            @RequestBody ReservationRequest request
    ) {
        ReservationRequest requestWithMemberId = new ReservationRequest(memberId, request.date(), request.timeId(),
                request.themeId());
        ReservationWaitingResponse response = reservationWaitingService.save(requestWithMemberId);
        URI location = URI.create("/reservations/waitings/" + response.id());
        return ResponseEntity.created(location).body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        reservationWaitingService.deleteReservationWaiting(id);
        return ResponseEntity.noContent().build();
    }
}
