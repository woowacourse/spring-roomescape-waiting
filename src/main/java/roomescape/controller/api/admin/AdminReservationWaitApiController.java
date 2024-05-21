package roomescape.controller.api.admin;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.domain.ReservationWait;
import roomescape.service.dto.response.reservationwait.ReservationWaitResponses;
import roomescape.service.reservationwait.ReservationWaitFindService;

@RestController
public class AdminReservationWaitApiController {

    private final ReservationWaitFindService reservationWaitFindService;

    public AdminReservationWaitApiController(ReservationWaitFindService reservationWaitFindService) {
        this.reservationWaitFindService = reservationWaitFindService;
    }

    @GetMapping("/admin/reservations/wait")
    public ResponseEntity<ReservationWaitResponses> getReservationWaits() {
        List<ReservationWait> reservationWaits = reservationWaitFindService.findReservationWaits();
        return ResponseEntity.ok(ReservationWaitResponses.from(reservationWaits));
    }
}
