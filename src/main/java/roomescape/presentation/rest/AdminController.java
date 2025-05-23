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
import roomescape.application.WaitingService;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.user.User;
import roomescape.domain.waiting.Waiting;
import roomescape.presentation.request.CreateReservationAdminRequest;
import roomescape.presentation.response.ReservationResponse;
import roomescape.presentation.response.UserResponse;
import roomescape.presentation.response.WaitingResponse;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final ReservationService reservationService;
    private final WaitingService waitingService;
    private final UserService userService;

    public AdminController(final ReservationService reservationService,
                           final WaitingService waitingService,
                           final UserService userService) {
        this.reservationService = reservationService;
        this.waitingService = waitingService;
        this.userService = userService;
    }

    @PostMapping("/reservations")
    @ResponseStatus(CREATED)
    public ReservationResponse createReservation(@RequestBody @Valid final CreateReservationAdminRequest request) {
        Reservation reservation = reservationService.saveReservation(request.userId(), request.date(), request.timeId(),
                request.themeId());
        return ReservationResponse.from(reservation);
    }

    @GetMapping("/users")
    public List<UserResponse> readAllUsers() {
        List<User> users = userService.findAllUsers();

        return users.stream()
                .map(UserResponse::from)
                .toList();
    }

    @GetMapping("/waitings")
    public List<WaitingResponse> readAllWaitings() {
        List<Waiting> waitings = waitingService.findAllWaitings();

        return waitings.stream()
                .map(WaitingResponse::from)
                .toList();
    }
}
