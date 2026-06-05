package roomescape.login;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.JwtTokenProvider;
import roomescape.common.api.ApiResponse;
import roomescape.login.dto.request.LoginRequest;
import roomescape.login.dto.response.LoginResponse;
import roomescape.member.domain.AuthenticatedMember;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class LoginController {

    private final LoginService loginService;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@RequestBody @Valid LoginRequest body) {
        AuthenticatedMember member = loginService.login(body.name(), body.password());
        String accessToken = jwtTokenProvider.generateAccessToken(member);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(LoginResponse.bearer(accessToken)));
    }

    @DeleteMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout() {
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
