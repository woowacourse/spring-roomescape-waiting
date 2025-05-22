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
import roomescape.presentation.request.CreateReservationAdminRequest;
import roomescape.presentation.response.ReservationResponse;
import roomescape.presentation.response.UserResponse;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final ReservationService reservationService;
    private final UserService userService;

    public AdminController(final ReservationService reservationService, final UserService userService) {
        this.reservationService = reservationService;
        this.userService = userService;
    }

    @PostMapping("/reservations")
    @ResponseStatus(CREATED)
    public ReservationResponse reserve(@RequestBody @Valid final CreateReservationAdminRequest request) {
        var user = userService.getById(request.userId());
        var reservation = reservationService.reserve(user, request.date(), request.timeId(), request.themeId());
        return ReservationResponse.from(reservation);
    }

    @GetMapping("/users")
    public List<UserResponse> getAllUsers() {
        var users = userService.findAllUsers();
        return users.stream()
                .map(UserResponse::from)
                .toList();
    }
}
