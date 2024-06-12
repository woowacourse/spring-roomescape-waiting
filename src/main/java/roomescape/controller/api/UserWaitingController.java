package roomescape.controller.api;

import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.controller.dto.CreateReservationResponse;
import roomescape.controller.dto.CreateUserReservationRequest;
import roomescape.domain.member.Member;
import roomescape.domain.reservation.Reservation;
import roomescape.service.WaitingService;
import roomescape.system.argumentresolver.AuthenticationPrincipal;

@RestController
public class UserWaitingController {

    private final WaitingService reservationService;

    public UserWaitingController(WaitingService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping("/reservations/waiting")
    public ResponseEntity<CreateReservationResponse> saveReservationWaiting(
        @Valid @RequestBody CreateUserReservationRequest request,
        @AuthenticationPrincipal Member member) {
        Reservation reservation = reservationService.saveWaiting(
            member.getId(),
            request.date(),
            request.timeId(),
            request.themeId()
        );

        return ResponseEntity.created(URI.create("/reservations/waiting/" + reservation.getId()))
            .body(CreateReservationResponse.from(reservation));
    }

    @DeleteMapping("/reservations-mine/{id}")
    public ResponseEntity<Void> deleteReservationWaiting(
        @PathVariable Long id,
        @AuthenticationPrincipal Member member) {
        reservationService.deleteWaiting(member, id);

        return ResponseEntity.noContent().build();
    }
}
