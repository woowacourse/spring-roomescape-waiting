package roomescape.api.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
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
import roomescape.api.dto.ReservationRequest;
import roomescape.api.dto.ReservationResponse;
import roomescape.api.dto.ReservationResponses;
import roomescape.api.dto.ReservationUpdateRequest;
import roomescape.application.ReservationApplicationService;
import roomescape.application.ReservationCancellationUseCase;
import roomescape.domain.Reservation;

@RestController
@RequestMapping("/reservations")
public class ReservationController {

    private final ReservationApplicationService reservationApplicationService;
    private final ReservationCancellationUseCase reservationCancellationUseCase;

    public ReservationController(
            ReservationApplicationService reservationApplicationService,
            ReservationCancellationUseCase reservationCancellationUseCase
    ) {
        this.reservationApplicationService = reservationApplicationService;
        this.reservationCancellationUseCase = reservationCancellationUseCase;
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> add(
            @RequestBody @Valid ReservationRequest request
    ) {
        Reservation reservation = reservationApplicationService.save(request);
        ReservationResponse response = ReservationResponse.from(reservation);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(response);
    }

    @PutMapping("/me/{id}")
    public ResponseEntity<ReservationResponse> updateMine(
            @PathVariable Long id,
            @RequestParam String name,
            @RequestBody @Valid ReservationUpdateRequest request
    ) {
        Reservation updated = reservationCancellationUseCase.updateMyReservation(id, name, request);

        return ResponseEntity.ok()
                .body(ReservationResponse.from(updated));
    }

    @GetMapping
    public ResponseEntity<ReservationResponses> search(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok()
                .body(reservationApplicationService.findPage(page, size));
    }

    @GetMapping("/me")
    public ResponseEntity<ReservationResponses> searchMine(
            @RequestParam String name
    ) {
        return ResponseEntity.ok()
                .body(reservationApplicationService.findMine(name));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id
    ) {
        reservationCancellationUseCase.deleteReservation(id);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/me/{id}")
    public ResponseEntity<Void> cancelMine(
            @PathVariable Long id,
            @RequestParam String name
    ) {
        reservationCancellationUseCase.deleteMyReservation(id, name);

        return ResponseEntity.noContent().build();
    }
}
