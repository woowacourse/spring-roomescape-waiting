package roomescape.presentation.reservation;

import jakarta.validation.Valid;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.application.reservation.ReservationService;
import roomescape.presentation.reservation.request.AdminReservationCreateRequest;
import roomescape.presentation.reservation.request.ReservationUpdateRequest;
import roomescape.presentation.reservation.response.ReservationCreateResponse;
import roomescape.presentation.reservation.response.ReservationUpdateResponse;
import roomescape.presentation.reservation.response.ReservationsResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/reservations")
public class AdminReservationController {

    private final ReservationService reservationService;

    @GetMapping
    public ResponseEntity<ReservationsResponse> getAllReservation() {
        return ResponseEntity.ok(reservationService.getAllReservations());
    }

    @PostMapping
    public ResponseEntity<ReservationCreateResponse> createReservation(
            @Valid @RequestBody AdminReservationCreateRequest request
    ) {
        ReservationCreateResponse response = reservationService.createReservationByAdmin(request);
        return ResponseEntity.created(URI.create("/reservations/" + response.id()))
                .body(response);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ReservationUpdateResponse> updateReservation(
            @PathVariable Long id,
            @Valid @RequestBody ReservationUpdateRequest request
    ) {
        ReservationUpdateResponse response = reservationService.updateReservationByAdmin(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservation(@PathVariable Long id) {
        reservationService.deleteReservationByAdmin(id);
        return ResponseEntity.noContent().build();
    }
}
