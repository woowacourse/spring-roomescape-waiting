package roomescape.reservation.ui;

import static roomescape.auth.domain.AuthRole.ADMIN;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.domain.MemberAuthInfo;
import roomescape.auth.domain.RequiresRole;
import roomescape.reservation.application.AdminReservationService;
import roomescape.reservation.ui.dto.request.CreateReservationRequest;
import roomescape.reservation.ui.dto.request.ReservationsByFilterRequest;
import roomescape.reservation.ui.dto.response.ReservationResponse;
import roomescape.reservation.ui.dto.response.ReservationStatusResponse;

@RestController
@RequestMapping("/admin")
@RequiresRole(authRoles = {ADMIN})
@RequiredArgsConstructor
public class AdminReservationRestController {

    private final AdminReservationService adminReservationService;

    @PostMapping("/reservations")
    public ResponseEntity<ReservationResponse> create(
            @RequestBody @Valid final CreateReservationRequest request
    ) {
        final ReservationResponse response = adminReservationService.createReservation(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(response);
    }

    @DeleteMapping("/reservations/{id}")
    public ResponseEntity<Void> deleteAsAdmin(
            @PathVariable final Long id,
            final MemberAuthInfo memberAuthInfo
    ) {
        adminReservationService.deleteAsAdmin(id, memberAuthInfo.authRole());

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/reservations")
    public ResponseEntity<List<ReservationResponse>> findAllReservations() {
        final List<ReservationResponse> reservationResponses = adminReservationService.findAll();

        return ResponseEntity.ok(reservationResponses);
    }

    @GetMapping("/reservations/filtered")
    public ResponseEntity<List<ReservationResponse>> findAllByFilter(
            @ModelAttribute @Valid final ReservationsByFilterRequest request
    ) {
        final List<ReservationResponse> reservationResponses = adminReservationService.findAllByFilter(request);

        return ResponseEntity.ok(reservationResponses);
    }

    @GetMapping("/reservations/statuses")
    @RequiresRole(authRoles = {ADMIN})
    public ResponseEntity<List<ReservationStatusResponse>> findAllReservationStatuses() {
        return ResponseEntity.ok()
                .body(adminReservationService.findAllReservationStatuses());
    }

    @DeleteMapping("/waitings/{id}")
    public ResponseEntity<Void> deny(
            @PathVariable final Long id,
            final MemberAuthInfo memberAuthInfo
    ) {
        adminReservationService.deleteAsAdmin(id, memberAuthInfo.authRole());

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/waitings")
    public ResponseEntity<List<ReservationResponse>> findAllWaitings() {
        final List<ReservationResponse> reservationResponses = adminReservationService.findAllWaitings();

        return ResponseEntity.ok(reservationResponses);
    }
}
