package roomescape.controller.admin.reservation;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import roomescape.service.reservation.ReservationDeleteService;

@Controller
@RequestMapping("/admin/reservations")
public class AdminReservationDeleteController {
    private final ReservationDeleteService reservationDeleteService;

    public AdminReservationDeleteController(final ReservationDeleteService reservationDeleteService) {
        this.reservationDeleteService = reservationDeleteService;
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        reservationDeleteService.delete(id);
        return ResponseEntity.noContent().build();
    }


    @DeleteMapping("/waiting/{id}")
    public ResponseEntity<Void> deleteWaiting(@PathVariable Long id) {
        reservationDeleteService.deleteWaitingById(id);
        return ResponseEntity.noContent().build();
    }
}
