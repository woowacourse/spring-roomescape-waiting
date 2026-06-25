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
import roomescape.application.ReservationApplicationService;
import roomescape.application.ReservationPayment;
import roomescape.domain.Reservation;
import roomescape.dto.MyReservationResponses;
import roomescape.dto.ReservationCreateResponse;
import roomescape.dto.ReservationRequest;
import roomescape.dto.ReservationResponse;
import roomescape.dto.ReservationResponses;
import roomescape.dto.ReservationUpdateRequest;
import roomescape.service.ReservationService;

@RestController
@RequestMapping("/reservations")
public class ReservationController {

    private final ReservationService reservationService;
    private final ReservationApplicationService reservationApplicationService;

    public ReservationController(ReservationService reservationService,
                                 ReservationApplicationService reservationApplicationService) {
        this.reservationService = reservationService;
        this.reservationApplicationService = reservationApplicationService;
    }

    @GetMapping
    public ResponseEntity<ReservationResponses> search(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok().body(reservationService.getReservationPage(page, size));
    }

    @GetMapping("/me")
    public ResponseEntity<MyReservationResponses> searchMine(
            @RequestParam String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok().body(reservationService.getMyReservations(name, page, size));
    }

    @PostMapping
    public ResponseEntity<ReservationCreateResponse> add(@RequestBody @Valid ReservationRequest request) {
        ReservationPayment created = reservationApplicationService.addReservation(request);
        ReservationCreateResponse response = ReservationCreateResponse.of(created.reservation(), created.payment());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        reservationApplicationService.deleteReservation(id);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/me/{id}")
    public ResponseEntity<Void> cancelMine(@PathVariable Long id, @RequestParam String name) {
        reservationApplicationService.cancelMyReservation(id, name);

        return ResponseEntity.noContent().build();
    }

    @PutMapping("/me/{id}")
    public ResponseEntity<ReservationResponse> updateMine(
            @PathVariable Long id,
            @RequestParam String name,
            @RequestBody @Valid ReservationUpdateRequest request
    ) {
        Reservation updated = reservationApplicationService.updateMyReservation(id, name, request);

        return ResponseEntity.ok(ReservationResponse.from(updated));
    }
}
