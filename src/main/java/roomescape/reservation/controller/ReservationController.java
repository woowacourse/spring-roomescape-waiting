package roomescape.reservation.controller;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.reservation.Reservation;
import roomescape.reservation.dto.ReservationChangeRequest;
import roomescape.reservation.dto.ReservationRequest;
import roomescape.reservation.dto.ReservationResponse;
import roomescape.reservation.service.ReservationService;

@Validated
@RestController
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping("/reservations")
    public ResponseEntity<List<ReservationResponse>> readAll() {
        List<ReservationResponse> reservations = reservationService.findAll().stream()
                .map(ReservationResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok().body(reservations);
    }

    @GetMapping("/reservations/{id}")
    public ResponseEntity<ReservationResponse> readById(@PathVariable Long id) {
        Reservation reservation = reservationService.findById(id);
        return ResponseEntity.ok().body(ReservationResponse.from(reservation));
    }

    @GetMapping(value = "/reservations/list", params = "name")
    public ResponseEntity<List<ReservationResponse>> readByName(@RequestParam String name) {
        List<ReservationResponse> reservations = reservationService.findByName(name).stream()
                .map(ReservationResponse::from)
                .toList();
        return ResponseEntity.ok().body(reservations);
    }

    @PostMapping("/reservations")
    public ResponseEntity<?> create(@Valid @RequestBody ReservationRequest request) {
        Reservation reservation = reservationService.add(
                request.name(),
                request.themeId(),
                request.date(),
                request.timeId()
        );

        URI location = URI.create("/reservation/" + reservation.getId());

        return ResponseEntity.created(location).body(ReservationResponse.from(reservation));
    }

    @PatchMapping("/reservations/{id}")
    public ResponseEntity<ReservationResponse> updateDateTimeByName(@PathVariable Long id,
                                                                    @Valid @RequestBody ReservationChangeRequest request) {
        Reservation reservation = reservationService.modifyDateTimeByName(
                id,
                request.name(),
                request.themeId(),
                request.date(),
                request.timeId()
        );

        return ResponseEntity.ok().body(ReservationResponse.from(reservation));
    }

    @DeleteMapping("/reservations/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        reservationService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping(value = "/reservations/my/{id}", params = "name")
    public ResponseEntity<Void> deleteByIdIfNameMatches(@PathVariable Long id, @RequestParam String name) {
        reservationService.deleteByIdIfNameMatches(id, name);
        return ResponseEntity.noContent().build();
    }
}
