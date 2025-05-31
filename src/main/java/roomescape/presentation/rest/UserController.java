package roomescape.presentation.rest;

import static org.springframework.http.HttpStatus.CREATED;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import roomescape.application.UserService;
import roomescape.domain.user.User;
import roomescape.presentation.auth.Authenticated;
import roomescape.presentation.request.SignupRequest;
import roomescape.presentation.response.UserReservedRecordsResponse;
import roomescape.presentation.response.UserResponse;

@RestController
public class UserController {

    private final UserService userService;

    public UserController(final UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/users")
    @ResponseStatus(CREATED)
    public UserResponse createUser(
            @RequestBody @Valid final SignupRequest request
    ) {
        User user = userService.saveUser(request.email(), request.password(), request.name());

        return UserResponse.fromUser(user);
    }

    @GetMapping("/admin/users")
    public List<UserResponse> readAllUsers() {
        List<User> users = userService.findAllUsers();

        return UserResponse.fromUsers(users);
    }

    @GetMapping("/users/reservations")
    public List<UserReservedRecordsResponse> readAllRecordByUser(
            @Authenticated final User user
    ) {
        return userService.findTotalRecordByUserId(user.id());
    }
}
