package roomescape.presentation.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.AuthRequired;
import roomescape.auth.AuthToken;
import roomescape.auth.LoginInfo;
import roomescape.business.application_service.reader.UserReader;
import roomescape.business.application_service.service.AuthService;
import roomescape.business.dto.UserDto;
import roomescape.presentation.dto.request.LoginRequest;
import roomescape.presentation.dto.response.UserResponse;

@RestController
@RequiredArgsConstructor
public class AuthApiController {

    private final AuthService authService;
    private final UserReader userReader;

    @PostMapping("/login")
    public ResponseEntity<Void> login(@RequestBody @Valid LoginRequest request) {
        AuthToken authToken = authService.authenticate(request.email(), request.password());
        return ResponseEntity.noContent().headers(authToken.toHttpHeaders()).build();
    }

    @GetMapping("/login/check")
    @AuthRequired
    public ResponseEntity<UserResponse> check(LoginInfo loginInfo) {
        UserDto userDto = userReader.getById(loginInfo.id());
        return ResponseEntity.ok(UserResponse.from(userDto));
    }
}
