package roomescape.presentation.rest;

import static org.springframework.http.HttpStatus.CREATED;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import roomescape.application.ReservationService;
import roomescape.application.UserService;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.user.User;
import roomescape.presentation.auth.Authenticated;
import roomescape.presentation.request.SignupRequest;
import roomescape.presentation.response.UserReservationResponse;
import roomescape.presentation.response.UserResponse;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final ReservationService reservationService;

    public UserController(final UserService userService, final ReservationService reservationService) {
        this.userService = userService;
        this.reservationService = reservationService;
    }

    @PostMapping
    @ResponseStatus(CREATED)
    public UserResponse register(@RequestBody @Valid final SignupRequest request) {
        User user = userService.register(request.email(), request.password(), request.name());

        return UserResponse.from(user);
    }

    @GetMapping("/reservations")
    public List<UserReservationResponse> getAllReservationsByUser(@Authenticated final User user) {
        List<Reservation> reservations = reservationService.getReservations(user.id());

        return reservations.stream()
                .map(UserReservationResponse::from)
                .toList();
    }
}
