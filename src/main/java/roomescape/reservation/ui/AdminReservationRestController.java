package roomescape.reservation.ui;

import static roomescape.auth.domain.AuthRole.ADMIN;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.domain.RequiresRole;
import roomescape.reservation.application.AdminReservationService;
import roomescape.reservation.ui.dto.request.CreateReservationRequest;
import roomescape.reservation.ui.dto.request.FilteredReservationsRequest;
import roomescape.reservation.ui.dto.response.ReservationResponse;
import roomescape.reservation.ui.dto.response.ReservationStatusResponse;

@RestController
@RequestMapping("/admin/reservations")
@RequiresRole(authRoles = {ADMIN})
@RequiredArgsConstructor
public class AdminReservationRestController {

    private final AdminReservationService adminReservationService;

    @PostMapping
    public ResponseEntity<ReservationResponse> create(
            @RequestBody @Valid final CreateReservationRequest request
    ) {
        final ReservationResponse response = adminReservationService.createReservation(request);

        return ResponseEntity.created(URI.create("/admin/reservations/" + response.id()))
                .body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAsAdmin(
            @PathVariable final Long id
    ) {
        adminReservationService.deleteAsAdmin(id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<ReservationResponse>> findAllReservations() {
        final List<ReservationResponse> reservationResponses = adminReservationService.findAll();

        return ResponseEntity.ok(reservationResponses);
    }

    @GetMapping("/filtered")
    public ResponseEntity<List<ReservationResponse>> findAllByFilter(
            @ModelAttribute @Valid final FilteredReservationsRequest request
    ) {
        final List<ReservationResponse> reservationResponses = adminReservationService.findAllByFilter(request);

        return ResponseEntity.ok(reservationResponses);
    }

    @GetMapping("/statuses")
    @RequiresRole(authRoles = {ADMIN})
    public ResponseEntity<List<ReservationStatusResponse>> findAllReservationStatuses() {
        return ResponseEntity.ok()
                .body(adminReservationService.findAllReservationStatuses());
    }
}
