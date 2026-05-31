package roomescape.controller;

import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.domain.User;
import roomescape.dto.user.command.CreateUserCommand;
import roomescape.dto.user.request.CreateUserRequest;
import roomescape.dto.user.response.UserResponse;
import roomescape.service.UserService;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<UserResponse> createUser(@RequestBody CreateUserRequest createUserRequest) {
        User createdUser = userService.register(CreateUserCommand.from(createUserRequest));
        URI location = URI.create("/users/" + createdUser.getId());
        return ResponseEntity.created(location).body(UserResponse.from(createdUser));
    }
}
