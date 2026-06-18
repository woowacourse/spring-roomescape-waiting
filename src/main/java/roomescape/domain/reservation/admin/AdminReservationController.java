package roomescape.domain.reservation.admin;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import roomescape.domain.reservation.ReservationService;
import roomescape.domain.reservation.admin.dto.ReservationResponse;
import roomescape.support.auth.AdminRequestValidator;

@RestController
@RequiredArgsConstructor
public class AdminReservationController {

    private final ReservationService reservationService;
    private final AdminRequestValidator validator;

    @GetMapping("/admin/waitings")
    public ResponseEntity<List<ReservationResponse>> getWaitingReservations(HttpServletRequest request) {
        if (validator.isUnauthorized(request)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        List<ReservationResponse> response = reservationService.getWaitingReservations();
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/admin/waitings/{id}")
    public ResponseEntity<Void> cancelReservation(HttpServletRequest request, @PathVariable Long id) {
        if (validator.isUnauthorized(request)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        reservationService.cancelReservationByAdmin(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
