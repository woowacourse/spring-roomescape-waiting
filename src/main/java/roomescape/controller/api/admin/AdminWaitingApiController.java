package roomescape.controller.api.admin;

import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.AuthenticatedMember;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.service.dto.response.WaitingResponse;
import roomescape.service.reservation.ReservationDeleteService;
import roomescape.service.reservation.ReservationFindService;

import java.util.List;

@RestController
public class AdminWaitingApiController {

    private final ReservationFindService reservationFindService;
    private final ReservationDeleteService reservationDeleteService;

    public AdminWaitingApiController(ReservationFindService reservationFindService,
                                     ReservationDeleteService reservationDeleteService) {
        this.reservationFindService = reservationFindService;
        this.reservationDeleteService = reservationDeleteService;
    }

    @GetMapping("/admin/waitings")
    public ResponseEntity<List<WaitingResponse>> getWaiting(@AuthenticatedMember Member member) {
        List<Reservation> waitings = reservationFindService.findWaitings();
        return ResponseEntity.ok(
                waitings.stream()
                        .map(WaitingResponse::new)
                        .toList()
        );
    }

    @DeleteMapping("/admin/waitings/{id}")
    public ResponseEntity<Void> deleteReservation(@AuthenticatedMember Member member,
                                                  @PathVariable
                                                  @Positive(message = "1 이상의 값만 입력해주세요.") long id) {
        reservationDeleteService.deleteReservation(id);
        return ResponseEntity.noContent().build();
    }
}
