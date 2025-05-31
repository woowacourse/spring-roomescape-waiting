package roomescape.reservation.ui;

import static roomescape.auth.domain.AuthRole.ADMIN;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.domain.RequiresRole;
import roomescape.reservation.application.ReservationService;
import roomescape.reservation.ui.dto.request.AdminCreateReservationRequest;
import roomescape.reservation.ui.dto.request.ReservationsByfilterRequest;
import roomescape.reservation.ui.dto.response.AdminReservationResponse;
import roomescape.reservation.ui.dto.response.AdminReservationWaitingResponse;

@RequestMapping("/admin")
@RequiresRole(authRoles = {ADMIN})
@RestController
@RequiredArgsConstructor
public class AdminReservationRestController {

    private final ReservationService reservationService;

    @PostMapping("/reservations")
    public ResponseEntity<AdminReservationResponse> create(
            @RequestBody @Valid final AdminCreateReservationRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reservationService.createForAdmin(request));
    }

    @GetMapping("/reservations")
    public ResponseEntity<List<AdminReservationResponse>> findAll() {
        final List<AdminReservationResponse> adminReservationResponse = reservationService.findAll();

        return ResponseEntity.ok(adminReservationResponse);
    }

    @GetMapping("/reservations/filtered")
    public ResponseEntity<List<AdminReservationResponse>> findAllByFilter(
            @ModelAttribute @Valid final ReservationsByfilterRequest request
    ) {
        final List<AdminReservationResponse> adminReservationResponse = reservationService.findAllByFilter(request);

        return ResponseEntity.ok(adminReservationResponse);
    }

    @GetMapping("/reservations/waiting")
    public ResponseEntity<List<AdminReservationWaitingResponse>> findAllWaitings() {
        return ResponseEntity.ok(reservationService.findReservationWaitings());
    }

}
