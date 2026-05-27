package roomescape.reservationWaiting.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import roomescape.reservationWaiting.dto.command.CreateReservationWaitingCommand;
import roomescape.reservationWaiting.dto.request.ReservationWaitingRequest;
import roomescape.reservationWaiting.dto.response.ReservationWaitingResponse;
import roomescape.reservationWaiting.service.ReservationWaitingService;

import java.net.URI;

@RestController
@RequestMapping("/waitings")
public class ReservationWaitingController {
    private static final String LOCATION_DEFAULT_VALUE = "/waitings/";

    private final ReservationWaitingService reservationWaitingService;

    public ReservationWaitingController(ReservationWaitingService reservationWaitingService) {
        this.reservationWaitingService = reservationWaitingService;
    }

    @PostMapping
    public ResponseEntity<ReservationWaitingResponse> addReservationWaiting(@Valid @RequestBody ReservationWaitingRequest request) {
        CreateReservationWaitingCommand command = new CreateReservationWaitingCommand(
                request.name(), request.reservationDate(), request.timeId(), request.themeId()
        );
        ReservationWaitingResponse response = reservationWaitingService.addReservationWaiting(command);
        return ResponseEntity.created(URI.create(LOCATION_DEFAULT_VALUE + response.id()))
                .body(response);
    }

    @DeleteMapping("/{waitingId}")
    public ResponseEntity<Void> deleteReservationWaiting(
            @PathVariable("waitingId") Long waitingId) {
        reservationWaitingService.delete(waitingId);
        return ResponseEntity.noContent().build();
    }
}
