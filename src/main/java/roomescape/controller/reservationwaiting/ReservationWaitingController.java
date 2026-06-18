package roomescape.controller.reservationwaiting;

import jakarta.validation.Valid;
import java.net.URI;
import java.time.LocalDateTime;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.controller.reservationwaiting.dto.ReservationWaitingCreateRequest;
import roomescape.controller.reservationwaiting.dto.ReservationWaitingResponse;
import roomescape.service.reservationwaiting.ReservationWaitingService;

@RestController
@RequestMapping("/waitings")
public class ReservationWaitingController {

    private final ReservationWaitingService reservationWaitingService;

    public ReservationWaitingController(final ReservationWaitingService reservationWaitingService) {
        this.reservationWaitingService = reservationWaitingService;
    }

    @PostMapping
    public ResponseEntity<ReservationWaitingResponse> createReservationWaiting(
            @Valid @RequestBody final ReservationWaitingCreateRequest reservationWaitingRequest
    ) {
        ReservationWaitingResponse reservationWaiting = ReservationWaitingResponse.from(
                reservationWaitingService.save(
                        reservationWaitingRequest.name(),
                        reservationWaitingRequest.date(),
                        reservationWaitingRequest.themeId(),
                        reservationWaitingRequest.timeId(),
                        LocalDateTime.now()
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
