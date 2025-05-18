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
import roomescape.application.UserService;
import roomescape.domain.auth.AuthenticationInfo;
import roomescape.presentation.request.SignupRequest;
import roomescape.presentation.response.UserReservationResponse;
import roomescape.presentation.response.UserResponse;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(final UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    @ResponseStatus(CREATED)
    public UserResponse register(@RequestBody @Valid final SignupRequest request) {
        var user = userService.register(request.email(), request.password(), request.name());
        return UserResponse.from(user);
    }

    @GetMapping("/reservations")
    public List<UserReservationResponse> getAllReservationsByUser(final AuthenticationInfo authenticationInfo) {
        var reservations = userService.getReservations(authenticationInfo.id());
        return UserReservationResponse.from(reservations);
    }
}
