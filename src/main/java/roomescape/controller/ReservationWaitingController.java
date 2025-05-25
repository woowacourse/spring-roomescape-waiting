package roomescape.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.service.ReservationWaitingService;
import roomescape.service.dto.ReservationWaitingRequest;
import roomescape.service.dto.ReservationWaitingResponse;

@RestController
@RequestMapping("/reservations-waiting")
@RequiredArgsConstructor
public class ReservationWaitingController {

    private final ReservationWaitingService reservationWaitingService;

    @PostMapping
    public ResponseEntity<ReservationWaitingResponse> addReservationWaiting(@RequestBody @Valid final ReservationWaitingRequest request, final Long memberId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reservationWaitingService.addReservationWaiting(request, memberId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> removeReservationWaiting(@PathVariable(name = "id") final long id) {
        reservationWaitingService.removeReservationWaiting(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
