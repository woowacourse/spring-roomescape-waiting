package roomescape.reservationhistory;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.LoginMember;
import roomescape.auth.Role;
import roomescape.member.Member;
import roomescape.reservationhistory.dto.ReservationHistoryResponse;

@RestController
@RequestMapping("/api/v1/admin/store/reservation-history")
public class ManagerReservationHistoryController {

    private final ReservationHistoryService reservationHistoryService;

    public ManagerReservationHistoryController(ReservationHistoryService reservationHistoryService) {
        this.reservationHistoryService = reservationHistoryService;
    }

    @GetMapping
    public ResponseEntity<List<ReservationHistoryResponse>> getStoreHistory(
            @LoginMember(role = Role.MANAGER) Member manager) {
        List<ReservationHistory> histories = reservationHistoryService.getStoreHistory(manager.getStoreId());
        return ResponseEntity.ok().body(ReservationHistoryResponse.fromAll(histories));
    }
}
