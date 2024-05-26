package roomescape.controller.api;

import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.AuthenticatedMember;
import roomescape.domain.Member;
import roomescape.service.reservation.ReservationService;

@RestController
public class WaitingApiController {

    private final ReservationService reservationService;

    public WaitingApiController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @DeleteMapping("/api/waitings/{id}")
    public ResponseEntity<Void> deleteWaiting(@AuthenticatedMember Member member,
                                              @PathVariable
                                              @Positive(message = "1 이상의 값만 입력해주세요.")
                                              long id) {
        reservationService.deleteReservation(id, member);
        return ResponseEntity.noContent().build();
    }
}
