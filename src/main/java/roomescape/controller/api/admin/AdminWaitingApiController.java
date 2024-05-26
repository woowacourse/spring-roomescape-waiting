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
import roomescape.service.reservation.ReservationService;

import java.util.List;

@RestController
public class AdminWaitingApiController {

    private final ReservationService reservationService;

    public AdminWaitingApiController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping("/api/admin/waitings")
    public ResponseEntity<List<WaitingResponse>> getWaiting(@AuthenticatedMember Member member) {
        List<Reservation> waitings = reservationService.findWaitings();
        return ResponseEntity.ok(
                waitings.stream()
                        .map(WaitingResponse::new)
                        .toList()
        );
    }

    @DeleteMapping("/api/admin/waitings/{id}")
    public ResponseEntity<Void> deleteReservation(@AuthenticatedMember Member member,
                                                  @PathVariable
                                                  @Positive(message = "1 이상의 값만 입력해주세요.") long id) {
        reservationService.deleteReservation(id, member);
        return ResponseEntity.noContent().build();
    }
}
