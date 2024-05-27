package roomescape.controller.api;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import roomescape.domain.Member;
import roomescape.dto.request.ReservationWaitingRequest;
import roomescape.dto.response.ReservationWaitingResponse;
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
            @RequestBody ReservationWaitingRequest request, Member member
    ) {
        ReservationWaitingResponse response = reservationWaitingService.create(request, member);
        URI location = URI.create("/reservations/waitings/" + response.id());
        return ResponseEntity.created(location).body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        reservationWaitingService.deleteReservationWaiting(id);
        return ResponseEntity.noContent().build();
    }
}
