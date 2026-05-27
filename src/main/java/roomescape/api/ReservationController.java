package roomescape.api;

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
import roomescape.domain.Reservation;
import roomescape.dto.ReservationRequest;
import roomescape.dto.ReservationResponse;
import roomescape.dto.ReservationResponses;
import roomescape.dto.ReservationUpdateRequest;
import roomescape.facade.ReservationFacade;
import roomescape.service.ReservationService;

@RestController
@RequestMapping("/reservations")
public class ReservationController {

    private final ReservationService reservationService;
    private final ReservationFacade reservationFacade;

    public ReservationController(
            ReservationService reservationService,
            ReservationFacade reservationFacade
    ) {
        this.reservationService = reservationService;
        this.reservationFacade = reservationFacade;
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> add(
            @RequestBody @Valid ReservationRequest request
    ) {
        Reservation reservation = reservationFacade.addReservation(request);
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
        Reservation updated = reservationFacade.updateMyReservation(id, name, request);

        return ResponseEntity.ok()
                .body(ReservationResponse.from(updated));
    }

    @GetMapping
    public ResponseEntity<ReservationResponses> search(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok()
                .body(reservationService.getReservationPage(page, size));
    }

    @GetMapping("/me")
    public ResponseEntity<ReservationResponses> searchMine(
            @RequestParam String name
    ) {
        return ResponseEntity.ok()
                .body(reservationService.getMyReservations(name));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id
    ) {
        reservationService.deleteReservation(id);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/me/{id}")
    public ResponseEntity<Void> cancelMine(
            @PathVariable Long id,
            @RequestParam String name
    ) {
        reservationService.cancelMyReservation(id, name);

        return ResponseEntity.noContent().build();
    }
}
