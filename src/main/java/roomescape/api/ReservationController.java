package roomescape.api;

import jakarta.validation.Valid;
import java.util.List;
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
import roomescape.dto.ReservationRequest;
import roomescape.dto.ReservationResponse;
import roomescape.dto.ReservationUpdateRequest;
import roomescape.service.ReservationService;

@RestController
@RequestMapping("/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping
    public ResponseEntity<List<ReservationResponse>> findAll() {
        List<ReservationResponse> responses = reservationService.getReservations().stream()
                .map(ReservationResponse::from)
                .toList();
        return ResponseEntity.ok().body(responses);
    }

    @GetMapping(params = "name")
    public ResponseEntity<List<ReservationResponse>> findByName(@RequestParam String name) {
        List<ReservationResponse> responses = reservationService.getReservationsByName(name).stream()
                .map(ReservationResponse::from)
                .toList();
        return ResponseEntity.ok().body(responses);
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> add(@Valid @RequestBody ReservationRequest request) {
        ReservationResponse response = ReservationResponse.from(reservationService.addReservation(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        reservationService.deleteReservation(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping(value = "/{id}", params = "name")
    public ResponseEntity<Void> cancelMyReservation(@PathVariable Long id, @RequestParam String name) {
        reservationService.cancelMyReservation(id, name);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping(value = "/{id}", params = "name")
    public ResponseEntity<ReservationResponse> update(@PathVariable Long id, @RequestParam String name,
                                                      @Valid @RequestBody ReservationUpdateRequest request) {
        ReservationResponse response = ReservationResponse.from(
                reservationService.updateReservation(id, name, request));
        return ResponseEntity.ok().body(response);
    }
}
