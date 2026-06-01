package roomescape.reservationwaiting.controller;

import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.ReservationApplicationService;
import roomescape.reservationwaiting.controller.dto.ReservationWaitingCreateRequest;
import roomescape.reservationwaiting.controller.dto.ReservationWaitingResponse;
import roomescape.reservationwaiting.service.ReservationWaitingService;

@RestController
@RequestMapping("/waitings")
public class ReservationWaitingController {

    private final ReservationWaitingService reservationWaitingService;
    private final ReservationApplicationService reservationApplicationService;

    public ReservationWaitingController(
            final ReservationWaitingService reservationWaitingService,
            final ReservationApplicationService reservationApplicationService
    ) {
        this.reservationWaitingService = reservationWaitingService;
        this.reservationApplicationService = reservationApplicationService;
    }

    @PostMapping
    public ResponseEntity<ReservationWaitingResponse> createReservationWaiting(
            @Valid @RequestBody final ReservationWaitingCreateRequest reservationWaitingRequest
    ) {
        ReservationWaitingResponse reservationWaiting = ReservationWaitingResponse.from(
                reservationApplicationService.saveWaiting(
                        reservationWaitingRequest.name(),
                        reservationWaitingRequest.date(),
                        reservationWaitingRequest.themeId(),
                        reservationWaitingRequest.timeId()
                ));

        return ResponseEntity.created(URI.create("/waitings/" + reservationWaiting.id()))
                .body(reservationWaiting);
    }

    @DeleteMapping("/{waiting_id}")
    public ResponseEntity<Void> deleteReservationWaiting(
            @PathVariable("waiting_id") final Long waitingId,
            @RequestParam final String name
    ) {
        reservationWaitingService.deleteByIdAndName(waitingId, name);
        return ResponseEntity.noContent().build();
    }
}
