package roomescape.presentation.api;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.AuthRequired;
import roomescape.auth.Role;
import roomescape.business.application_service.reader.UserReader;
import roomescape.business.application_service.service.UserService;
import roomescape.business.dto.UserDto;
import roomescape.business.model.vo.UserRole;
import roomescape.presentation.dto.request.RegisterRequest;
import roomescape.presentation.dto.response.UserResponse;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class UserApiController {

    private final UserService userService;
    private final UserReader userReader;

    @GetMapping("/members")
    @AuthRequired
    @Role(UserRole.ADMIN)
    public ResponseEntity<List<UserResponse>> getUsers() {
        List<UserDto> users = userReader.getAll();
        List<UserResponse> responses = UserResponse.from(users);
        return ResponseEntity.ok(responses);
    }

    @PostMapping("/members")
    public ResponseEntity<UserResponse> register(@RequestBody RegisterRequest request) {
        final UserDto user = userService.register(request.name(), request.email(), request.password());
        final UserResponse response = UserResponse.from(user);
        final URI uri = URI.create("/members");
        return ResponseEntity.created(uri).body(response);
    }
}
