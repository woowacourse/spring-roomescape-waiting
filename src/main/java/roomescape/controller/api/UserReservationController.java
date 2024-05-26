package roomescape.controller.api;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.controller.dto.CreateReservationResponse;
import roomescape.controller.dto.CreateUserReservationRequest;
import roomescape.controller.dto.FindMyReservationResponse;
import roomescape.domain.member.Member;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationWithRank;
import roomescape.service.ReservationService;
import roomescape.system.argumentresolver.AuthenticationPrincipal;

@RestController
public class UserReservationController {

    private final ReservationService reservationService;

    public UserReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping("/reservations")
    public ResponseEntity<CreateReservationResponse> save(
        @Valid @RequestBody CreateUserReservationRequest request,
        @AuthenticationPrincipal Member member) {

        Reservation reservation = reservationService.save(
            member.getId(),
            request.date(),
            request.timeId(),
            request.themeId()
        );

        return ResponseEntity.created(URI.create("/reservations/" + reservation.getId()))
            .body(CreateReservationResponse.from(reservation));
    }

    @GetMapping("/reservations-mine")
    public ResponseEntity<List<FindMyReservationResponse>> findMyReservations(
        @AuthenticationPrincipal Member member) {
        List<ReservationWithRank> reservationWithRanks = reservationService.findMyReservations(member.getId());
        List<FindMyReservationResponse> response = reservationWithRanks.stream()
            .map(FindMyReservationResponse::from)
            .toList();

        return ResponseEntity.ok(response);
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
