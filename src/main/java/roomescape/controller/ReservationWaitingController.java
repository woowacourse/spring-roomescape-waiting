package roomescape.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import roomescape.dto.command.CreateReservationWaitingCommand;
import roomescape.dto.request.ReservationWaitingRequest;
import roomescape.dto.response.ReservationWaitingResponse;
import roomescape.service.ReservationWaitingService;

import java.net.URI;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/waitings")
public class ReservationWaitingController {
    private static final String LOCATION_DEFAULT_VALUE = "/waitings/";

    private final ReservationWaitingService reservationWaitingService;

    public ReservationWaitingController(ReservationWaitingService reservationWaitingService) {
        this.reservationWaitingService = reservationWaitingService;
    }

    @PostMapping
    public ResponseEntity<ReservationWaitingResponse> createReservationWaiting(@Valid @RequestBody ReservationWaitingRequest request) {
        CreateReservationWaitingCommand command = new CreateReservationWaitingCommand(
                request.memberId(), request.reservationDate(), request.timeId(), request.themeId()
        );
        ReservationWaitingResponse response = reservationWaitingService.createReservationWaiting(command, LocalDateTime.now());
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
