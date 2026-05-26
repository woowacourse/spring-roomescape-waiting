package roomescape.domain.reservationslot.admin;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import roomescape.domain.reservationslot.ReservationSlotService;
import roomescape.domain.reservationslot.admin.dto.ReservationSlotResponse;
import roomescape.support.auth.AdminRequestValidator;

@RestController
@RequiredArgsConstructor
public class AdminReservationSlotController {

    private final ReservationSlotService reservationService;
    private final AdminRequestValidator validator;

    @GetMapping("/admin/reservations")
    public ResponseEntity<List<ReservationSlotResponse>> getAllReservation(HttpServletRequest request) {
        if (validator.isUnauthorized(request)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        List<ReservationSlotResponse> response = reservationService.getAllReservations();
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/admin/reservations/{id}")
    public ResponseEntity<Void> cancelReservation(HttpServletRequest request, @PathVariable Long id) {
        if (validator.isUnauthorized(request)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        reservationService.cancelReservationByAdmin(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
