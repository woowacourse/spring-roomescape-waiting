package roomescape.presentation.reservation;

import jakarta.validation.Valid;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.application.reservation.ReservationService;
import roomescape.common.auth.LoginUser;
import roomescape.domain.user.User;
import roomescape.presentation.reservation.request.ReservationCreateRequest;
import roomescape.presentation.reservation.request.ReservationUpdateRequest;
import roomescape.presentation.reservation.response.ReservationCreateResponse;
import roomescape.presentation.reservation.response.ReservationUpdateResponse;
import roomescape.presentation.reservation.response.UserReservationsResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    @GetMapping
    public ResponseEntity<UserReservationsResponse> getUserReservations(@LoginUser User loginUser) {
        UserReservationsResponse response = reservationService.getUserReservations(loginUser);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<ReservationCreateResponse> createReservation(
            @Valid @RequestBody ReservationCreateRequest request,
            @LoginUser User loginUser
    ) {
        ReservationCreateResponse response = reservationService.createReservationByUser(request, loginUser);
        return ResponseEntity.created(URI.create("/reservations/" + response.id()))
                .body(response);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ReservationUpdateResponse> updateReservation(
            @PathVariable Long id,
            @RequestBody ReservationUpdateRequest request,
            @LoginUser User loginUser
    ) {
        ReservationUpdateResponse response = reservationService.updateReservationByUser(id, request, loginUser);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelReservation(
            @PathVariable Long id,
            @LoginUser User loginUser
    ) {
        reservationService.cancelReservationByUser(id, loginUser);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
