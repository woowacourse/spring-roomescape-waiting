package roomescape.reservation.ui;

import static roomescape.auth.domain.AuthRole.ADMIN;
import static roomescape.auth.domain.AuthRole.MEMBER;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.domain.RequiresRole;
import roomescape.reservation.application.ReservationStatusService;
import roomescape.reservation.ui.dto.response.ReservationStatusResponse;

@RestController
@RequiredArgsConstructor
public class ReservationStatusRestController {

    private final ReservationStatusService reservationStatusService;

    @GetMapping("/reservation-statuses")
    @RequiresRole(authRoles = {ADMIN, MEMBER})
    public ResponseEntity<List<ReservationStatusResponse>> findAll() {
        return ResponseEntity.ok()
                .body(reservationStatusService.findAll());
    }
}
