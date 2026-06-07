package roomescape.presentation.reservation;

import jakarta.validation.Valid;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.application.reservation.ReservationService;
import roomescape.application.reservation.request.ReservationCreateRequest;
import roomescape.application.reservation.request.UserReservationUpdateRequest;
import roomescape.application.reservation.response.UserReservationCreateResponse;
import roomescape.application.reservation.response.UserReservationUpdateResponse;
import roomescape.application.reservation.response.UserReservationsResponse;
import roomescape.common.auth.LoginUser;
import roomescape.domain.user.User;

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
    public ResponseEntity<UserReservationCreateResponse> createReservation(
            @Valid @RequestBody ReservationCreateRequest request,
            @LoginUser User loginUser
    ) {
        UserReservationCreateResponse response = reservationService.createReservationByUser(request, loginUser);
        return ResponseEntity.created(URI.create("/reservations/" + response.id()))
                .body(response);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<UserReservationUpdateResponse> updateReservation(
            @PathVariable Long id,
            @RequestBody UserReservationUpdateRequest request,
            @LoginUser User loginUser
    ) {
        UserReservationUpdateResponse response = reservationService.updateReservationByUser(id, request, loginUser);
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
