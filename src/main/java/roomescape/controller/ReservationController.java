package roomescape.controller;

import jakarta.validation.Valid;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.dto.request.ReservationRequest;
import roomescape.dto.request.UpdateReservationRequest;
import roomescape.dto.response.MyReservationResponse;
import roomescape.dto.response.ReservationResponse;
import roomescape.service.ReservationService;

@RestController
@RequestMapping("/reservations")
public class ReservationController {

    private static final String LOCATION_DEFAULT_VALUE = "/reservations/";

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> create(@Valid @RequestBody ReservationRequest request) {
        ReservationResponse response = reservationService.create(request, LocalDateTime.now());
        return ResponseEntity.created(URI.create(LOCATION_DEFAULT_VALUE + response.id()))
                .body(response);
    }

    @GetMapping
    public ResponseEntity<List<ReservationResponse>> readReservations(@RequestParam(required = false, value = "name") String name) {
        if (name == null || name.isBlank()) {
            List<ReservationResponse> responses = reservationService.getReservations();
            return ResponseEntity.ok(responses);
        }
        List<ReservationResponse> responses = reservationService.getReservationsByName(name);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/mine")
    public ResponseEntity<List<MyReservationResponse>> getMyReservations(
            @RequestParam String name
    ) {
        List<MyReservationResponse> responses = reservationService.getMyReservations(name);
        return ResponseEntity.ok(responses);
    }

    @PutMapping("/{reservationId}")
    public ResponseEntity<ReservationResponse> update(@PathVariable("reservationId") long reservationId,
                                                      @Valid @RequestBody UpdateReservationRequest request) {
        ReservationResponse response = reservationService.update(reservationId, request, LocalDateTime.now());
        return ResponseEntity.ok(response);

    }

    @DeleteMapping("/{reservationId}")
    public ResponseEntity<Void> delete(@PathVariable("reservationId") long reservationId) {
        reservationService.delete(reservationId, LocalDateTime.now());
        return ResponseEntity.noContent()
                .build();
    }
}
