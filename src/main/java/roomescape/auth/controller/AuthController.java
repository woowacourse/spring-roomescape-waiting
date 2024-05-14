package roomescape.auth.controller;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.domain.AuthInfo;
import roomescape.auth.dto.request.LoginRequest;
import roomescape.auth.dto.response.GetAuthInfoResponse;
import roomescape.auth.dto.response.LoginResponse;
import roomescape.auth.service.AuthService;
import roomescape.auth.core.AuthenticationPrincipal;
import roomescape.auth.core.AuthorizationManager;

@RestController
public class AuthController {

    private final AuthorizationManager authorizationManager;
    private final AuthService authService;

    public AuthController(final AuthorizationManager authorizationManager, final AuthService authService) {
        this.authorizationManager = authorizationManager;
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<Void> login(HttpServletResponse httpServletResponse, @RequestBody @Valid LoginRequest loginMemberRequest) {
        LoginResponse loginResponse = authService.login(loginMemberRequest);
        authorizationManager.setAuthorization(httpServletResponse, loginResponse.token());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/login/check")
    public ResponseEntity<GetAuthInfoResponse> getMemberAuthInfo(@AuthenticationPrincipal AuthInfo authInfo) {
        GetAuthInfoResponse getAuthInfoResponse = authService.getMemberAuthInfo(authInfo);
        return ResponseEntity.ok(getAuthInfoResponse);
    }
}
