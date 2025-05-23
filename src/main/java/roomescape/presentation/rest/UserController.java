package roomescape.presentation.rest;

import static org.springframework.http.HttpStatus.CREATED;

import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import roomescape.application.ReservationService;
import roomescape.application.UserService;
import roomescape.application.WaitingService;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.user.User;
import roomescape.domain.waiting.WaitingWithRank;
import roomescape.presentation.auth.Authenticated;
import roomescape.presentation.request.SignupRequest;
import roomescape.presentation.response.UserReservationResponse;
import roomescape.presentation.response.UserResponse;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final ReservationService reservationService;
    private final WaitingService waitingService;

    public UserController(final UserService userService,
                          final ReservationService reservationService,
                          final WaitingService waitingService) {
        this.userService = userService;
        this.reservationService = reservationService;
        this.waitingService = waitingService;
    }

    @PostMapping
    @ResponseStatus(CREATED)
    public UserResponse createUser(@RequestBody @Valid final SignupRequest request) {
        User user = userService.saveUser(request.email(), request.password(), request.name());

        return UserResponse.from(user);
    }

    @GetMapping("/reservations")
    public List<UserReservationResponse> readAllReservationsByUser(@Authenticated final User user) {
        List<Reservation> reservations = reservationService.findReservationsByUserId(user.id());
        List<WaitingWithRank> waitings = waitingService.findWaitingByUserId(user.id());

        List<UserReservationResponse> userReservationResponses = new ArrayList<>();

        List<UserReservationResponse> reservedResponse = reservations.stream()
                .map(UserReservationResponse::fromReservation)
                .toList();

        List<UserReservationResponse> waitingResponse = waitings.stream()
                .map(UserReservationResponse::fromWaitingWithRank)
                .toList();

        userReservationResponses.addAll(reservedResponse);
        userReservationResponses.addAll(waitingResponse);

        return userReservationResponses;
    }
}
