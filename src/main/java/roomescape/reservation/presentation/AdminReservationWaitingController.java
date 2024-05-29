package roomescape.reservation.presentation;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.member.domain.Member;
import roomescape.reservation.application.ReservationManageService;
import roomescape.reservation.application.WaitingQueryService;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.dto.response.ReservationResponse;

import java.util.List;

@RestController
@RequestMapping("/admin/reservations/waiting")
public class AdminReservationWaitingController {
    private final WaitingQueryService waitingQueryService;
    private final ReservationManageService waitingScheduler;

    public AdminReservationWaitingController(WaitingQueryService waitingQueryService,
                                             @Qualifier("waitingManageService") ReservationManageService waitingScheduler) {
        this.waitingQueryService = waitingQueryService;
        this.waitingScheduler = waitingScheduler;
    }

    @GetMapping
    public ResponseEntity<List<ReservationResponse>> findWaitingReservations() {
        List<Reservation> waitingReservations = waitingQueryService.findAll();
        return ResponseEntity.ok(waitingReservations.stream()
                .map(ReservationResponse::from)
                .toList());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWaitingReservation(@PathVariable Long id, Member loginAdminMember) {
        waitingScheduler.delete(id, loginAdminMember);
        return ResponseEntity.noContent().build();
    }
}
