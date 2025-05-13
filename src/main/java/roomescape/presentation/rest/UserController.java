package roomescape.presentation.rest;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import roomescape.application.UserService;
import roomescape.presentation.request.SignupRequest;
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
        return ResponseEntity.ok(response);
    }
}
