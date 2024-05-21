package roomescape.controller.api.admin;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import roomescape.domain.ReservationWait;
import roomescape.service.dto.response.reservationwait.ReservationWaitResponses;
import roomescape.service.reservationwait.ReservationWaitDeleteService;
import roomescape.service.reservationwait.ReservationWaitFindService;

@RestController
public class AdminReservationWaitApiController {

    private final ReservationWaitFindService reservationWaitFindService;
    private final ReservationWaitDeleteService reservationWaitDeleteService;

    public AdminReservationWaitApiController(ReservationWaitFindService reservationWaitFindService,
                                             ReservationWaitDeleteService reservationWaitDeleteService) {
        this.reservationWaitFindService = reservationWaitFindService;
        this.reservationWaitDeleteService = reservationWaitDeleteService;
    }

    @GetMapping("/admin/reservations/wait")
    public ResponseEntity<ReservationWaitResponses> getReservationWaits() {
        List<ReservationWait> reservationWaits = reservationWaitFindService.findReservationWaits();
        return ResponseEntity.ok(ReservationWaitResponses.from(reservationWaits));
    }

    @DeleteMapping("/admin/reservations/{reservationWaitId}")
    public ResponseEntity<Void> deleteReservationWait(@PathVariable long reservationWaitId) {
        reservationWaitDeleteService.deleteById(reservationWaitId);
        return ResponseEntity.noContent().build();
    }
}
