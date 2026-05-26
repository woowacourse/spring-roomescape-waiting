package roomescape.controller.reservationwaiting;

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
import roomescape.controller.reservationwaiting.dto.ReservationWaitingCreateRequest;
import roomescape.controller.reservationwaiting.dto.ReservationWaitingResponse;

@RestController
@RequestMapping("/waitings")
public class ReservationWaitingController {
    @PostMapping
    public ResponseEntity<ReservationWaitingResponse> createReservationWaiting(@Valid @RequestBody ReservationWaitingCreateRequest reservationWaitingRequest) {

        ReservationWaitingResponse reservationWaiting = ReservationWaitingResponse.from(
                reservationWaitingService.save(
                reservationWaitingRequest.name(),
                reservationWaitingRequest.date(),
                reservationWaitingRequest.themeId(),
                reservationWaitingRequest.timeId()
        ));

        return ResponseEntity.created(URI.create("/waitings/" + reservationWaiting.id()))
                .body(reservationWaiting);
    }

    @DeleteMapping("/{waiting_id}")
    public ResponseEntity<Void> deleteReservationWaiting(@PathVariable("waiting_id") Long waitingId, @RequestParam String name) {

    }
}
