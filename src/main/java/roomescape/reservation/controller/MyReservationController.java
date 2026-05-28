package roomescape.reservation.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.annotation.Authorized;
import roomescape.auth.annotation.LoginName;
import roomescape.reservation.controller.dto.ReservationUpdateRequest;
import roomescape.reservation.service.ReservationService;

@RestController
@RequestMapping("/reservations")
public class MyReservationController {

    private final ReservationService reservationService;

    public MyReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @Authorized
    @PatchMapping("/{id}")
    public ResponseEntity<Void> updateMyReservation(
            @LoginName String name,
            @PathVariable Long id,
            @RequestBody ReservationUpdateRequest request
    ) {
        reservationService.validateReservationOwnership(id, name);
        reservationService.updateReservation(request.toCommand(), id);
        return ResponseEntity.noContent().build();
    }

    @Authorized
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMyReservation(
            @LoginName String name,
            @PathVariable Long id
    ) {
        reservationService.validateReservationOwnership(id, name);
        reservationService.validateReservationNotExpired(id);
        reservationService.deleteReservationById(id);

        return ResponseEntity.noContent().build();
    }
}
