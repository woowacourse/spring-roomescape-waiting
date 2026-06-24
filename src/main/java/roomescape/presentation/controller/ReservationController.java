package roomescape.presentation.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.application.ReservationApplicationService;
import roomescape.application.ReservationModificationUseCase;
import roomescape.domain.Reservation;
import roomescape.presentation.dto.ReservationResponse;
import roomescape.presentation.dto.ReservationResponses;
import roomescape.presentation.dto.ReservationUpdateRequest;

@RestController
@RequestMapping("/reservations")
public class ReservationController {

    private final ReservationApplicationService reservationApplicationService;
    private final ReservationModificationUseCase reservationModificationUseCase;

    public ReservationController(
            ReservationApplicationService reservationApplicationService,
            ReservationModificationUseCase reservationModificationUseCase
    ) {
        this.reservationApplicationService = reservationApplicationService;
        this.reservationModificationUseCase = reservationModificationUseCase;
    }

    @PutMapping("/me/{id}")
    public ResponseEntity<ReservationResponse> updateMine(
            @PathVariable Long id,
            @RequestParam String name,
            @RequestBody @Valid ReservationUpdateRequest request
    ) {
        Reservation updated = reservationModificationUseCase.updateMyReservation(id, name, request);

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
        reservationModificationUseCase.deleteReservation(id);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/me/{id}")
    public ResponseEntity<Void> cancelMine(
            @PathVariable Long id,
            @RequestParam String name
    ) {
        reservationModificationUseCase.deleteMyReservation(id, name);

        return ResponseEntity.noContent().build();
    }
}
