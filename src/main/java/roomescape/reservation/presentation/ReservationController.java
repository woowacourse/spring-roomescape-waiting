package roomescape.reservation.presentation;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.reservation.application.ReservationService;
import roomescape.reservation.presentation.dto.ReservationChangeRequest;
import roomescape.reservation.presentation.dto.ReservationPendingResponse;
import roomescape.reservation.presentation.dto.ReservationRequest;
import roomescape.reservation.presentation.dto.ReservationResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    @GetMapping
    public ResponseEntity<List<ReservationPendingResponse>> getReservationsByName(@RequestParam final String username) {
        List<ReservationPendingResponse> responses = reservationService.getReservationsByName(username)
                .stream()
                .map(ReservationPendingResponse::from)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> createReservation(@Valid @RequestBody final ReservationRequest request) {
        ReservationResponse response = ReservationResponse.from(reservationService.addReservation(request.toCommand()));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservation(@PathVariable final Long id, @RequestParam final String username) {
        reservationService.cancelReservation(id, username);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/active")
    public ResponseEntity<ReservationResponse> changeReservation(@PathVariable final Long id, @Valid @RequestBody final ReservationChangeRequest request) {
        ReservationResponse response = ReservationResponse.from(reservationService.changeReservation(id, request.toCommand()));
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/pending")
    public ResponseEntity<ReservationResponse> pendingReservation(@PathVariable final Long id, @Valid @RequestBody final ReservationChangeRequest request){
        ReservationResponse response = ReservationResponse.from(reservationService.changeReservationPendingStatus(id, request.toCommand()));
        return ResponseEntity.ok(response);
    }
}
