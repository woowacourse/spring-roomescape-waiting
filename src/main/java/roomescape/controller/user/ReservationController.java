package roomescape.controller.user;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.controller.dto.ReservationRequest;
import roomescape.controller.dto.ReservationResponse;
import roomescape.controller.dto.ReservationUpdateRequest;
import roomescape.domain.Reservation;
import roomescape.service.ReservationService;

import java.net.URI;
import java.util.List;

@Validated
@RestController
@RequestMapping("/reservations")
public class ReservationController {

    private final ReservationService service;

    public ReservationController(ReservationService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> createReservation(@Valid @RequestBody ReservationRequest request) {
        Reservation reservation = service.create(
                request.name(),
                request.date(),
                request.timeId(),
                request.themeId());
        return ResponseEntity.created(URI.create("/reservations/" + reservation.getId()))
                .body(ReservationResponse.from(reservation));
    }

    @GetMapping
    public ResponseEntity<List<ReservationResponse>> getReservationsByName(
            @RequestParam("name") @NotBlank(message = "name은 비어 있을 수 없습니다.") String name) {
        List<ReservationResponse> reservations = service.findByName(name).stream()
                .map(ReservationResponse::from)
                .toList();
        return ResponseEntity.ok(reservations);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservation(
            @PathVariable @Positive(message = "id는 양수이어야 합니다.") Long id,
            @RequestParam("name") @NotBlank(message = "name은 비어 있을 수 없습니다.") String name) {
        service.delete(id, name);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReservationResponse> updateReservation(
            @PathVariable @Positive(message = "id는 양수이어야 합니다.") Long id,
            @Valid @RequestBody ReservationUpdateRequest request) {
        Reservation reservation = service.update(
                id,
                request.name(),
                request.date(),
                request.timeId());
        return ResponseEntity.ok(ReservationResponse.from(reservation));
    }
}
