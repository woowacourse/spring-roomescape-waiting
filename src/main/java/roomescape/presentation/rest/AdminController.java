package roomescape.presentation.rest;

import static org.springframework.http.HttpStatus.CREATED;

import jakarta.validation.Valid;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import roomescape.application.ReservationService;
import roomescape.application.UserService;
import roomescape.presentation.request.CreateReservationAdminRequest;
import roomescape.presentation.response.ReservationResponse;
import roomescape.presentation.response.ReservationWithOrderResponse;
import roomescape.presentation.response.UserResponse;

@RestController
@RequestMapping("/admin")
@AllArgsConstructor
public class AdminController {

    private final ReservationService reservationService;
    private final UserService userService;

    @PostMapping("/reservations")
    @ResponseStatus(CREATED)
    public ReservationResponse reserve(@RequestBody @Valid final CreateReservationAdminRequest request) {
        var reservation = reservationService.reserve(request.userId(), request.date(), request.timeId(), request.themeId());
        return ReservationResponse.from(reservation);
    }

    @GetMapping("/users")
    public List<UserResponse> getAllUsers() {
        var users = userService.findAllUsers();
        return UserResponse.from(users);
    }

    @GetMapping("/waitings")
    public List<ReservationWithOrderResponse> getAllWaitings() {
        var waitings = reservationService.findAllWaitings();
        return ReservationWithOrderResponse.from(waitings);
    }
}
