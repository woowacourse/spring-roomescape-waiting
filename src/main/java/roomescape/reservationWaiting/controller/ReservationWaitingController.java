package roomescape.reservationWaiting.controller;

import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.annotation.Authorized;
import roomescape.auth.annotation.LoginName;
import roomescape.reservationWaiting.controller.dto.ReservationWaitingRequest;
import roomescape.reservationWaiting.controller.dto.ReservationWaitingResponse;
import roomescape.reservationWaiting.domain.ReservationWaiting;
import roomescape.reservationWaiting.service.ReservationWaitingService;

@RestController
@RequestMapping("/reservation-waitings")
public class ReservationWaitingController {

    private final ReservationWaitingService reservationWaitingService;

    public ReservationWaitingController(ReservationWaitingService reservationWaitingService) {
        this.reservationWaitingService = reservationWaitingService;
    }

    @PostMapping
    public ResponseEntity<ReservationWaitingResponse> createReservationWaiting(
            @RequestBody ReservationWaitingRequest request) {
        ReservationWaiting reservationWaiting = reservationWaitingService.makeReservationWaiting(request.toCommand());
        ReservationWaitingResponse response = ReservationWaitingResponse.from(reservationWaiting);

        return ResponseEntity
                .created(URI.create("/reservation-waitings/" + response.id()))
                .body(response);
    }

    @Authorized
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMyReservationWaiting(
            @LoginName String name, @PathVariable Long id) {
        reservationWaitingService.validateReservationWaitingOwnership(id, name);
        reservationWaitingService.deleteReservationWaitingById(id);

        return ResponseEntity.noContent().build();
    }
}
