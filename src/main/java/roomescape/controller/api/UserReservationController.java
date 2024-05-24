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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.controller.dto.CreateReservationResponse;
import roomescape.controller.dto.CreateUserReservationRequest;
import roomescape.controller.dto.CreateUserReservationStandbyRequest;
import roomescape.controller.dto.FindMyReservationResponse;
import roomescape.domain.member.Member;
import roomescape.domain.reservation.Reservation;
import roomescape.global.argumentresolver.AuthenticationPrincipal;
import roomescape.service.UserReservationService;
import roomescape.service.dto.FindReservationWithRankDto;

@RestController
@RequestMapping("/reservations")
public class UserReservationController {

    private final UserReservationService userReservationService;

    public UserReservationController(UserReservationService userReservationService) {
        this.userReservationService = userReservationService;
    }

    @PostMapping
    public ResponseEntity<CreateReservationResponse> save(
        @Valid @RequestBody CreateUserReservationRequest request,
        @AuthenticationPrincipal Member member) {

        Reservation reservation = userReservationService.reserve(
            member.getId(),
            request.date(),
            request.timeId(),
            request.themeId()
        );

        return ResponseEntity.created(URI.create("/reservations/" + reservation.getId()))
            .body(CreateReservationResponse.from(reservation));
    }

    @PostMapping("/standby")
    public ResponseEntity<CreateReservationResponse> standby(
        @Valid @RequestBody CreateUserReservationStandbyRequest request,
        @AuthenticationPrincipal Member member) {

        Reservation reservation = userReservationService.standby(
            member.getId(),
            request.date(),
            request.timeId(),
            request.themeId()
        );

        return ResponseEntity.created(URI.create("/reservations/" + reservation.getId()))
            .body(CreateReservationResponse.from(reservation));
    }

    @DeleteMapping("/standby/{id}")
    public ResponseEntity<Void> deleteStandby(@PathVariable Long id, @AuthenticationPrincipal Member member) {
        userReservationService.deleteStandby(id, member);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/mine")
    public ResponseEntity<List<FindMyReservationResponse>> findMyReservations(@AuthenticationPrincipal Member member) {
        List<FindReservationWithRankDto> reservations = userReservationService.findMyReservationsWithRank(member.getId());
        List<FindMyReservationResponse> response = reservations.stream()
            .map(data -> FindMyReservationResponse.from(data.reservation(), data.rank()))
            .toList();
        return ResponseEntity.ok(response);
    }
}
