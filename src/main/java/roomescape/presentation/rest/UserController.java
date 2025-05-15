package roomescape.presentation.rest;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import roomescape.application.UserService;
import roomescape.domain.user.User;
import roomescape.presentation.auth.Authenticated;
import roomescape.presentation.request.SignupRequest;
import roomescape.presentation.response.UserReservationResponse;
import roomescape.presentation.response.UserResponse;

@Controller
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(final UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<UserResponse> register(@RequestBody @Valid final SignupRequest request) {
        var user = userService.register(request.email(), request.password(), request.name());
        var response = UserResponse.from(user);
        return ResponseEntity.created(URI.create("/users/" + user.id())).body(response);
    }

    @GetMapping("/reservations")
    public ResponseEntity<List<UserReservationResponse>> getAllReservationsByUser(@Authenticated final User user) {
        var reservations = userService.getReservations(user.id());
        var response = UserReservationResponse.from(reservations);
        return ResponseEntity.ok(response);
    }

}
