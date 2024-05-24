package roomescape.controller.api.admin;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.domain.reservationwait.ReservationWait;
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

    @PutMapping("/admin/reservations/{reservationWaitId}")
    public ResponseEntity<Void> cancelReservationWait(@PathVariable long reservationWaitId) {
        reservationWaitDeleteService.cancelById(reservationWaitId);
        return ResponseEntity.noContent().build();
    }
}
