package roomescape.auth.login.presentation;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.admin.domain.Admin;
import roomescape.auth.login.presentation.dto.LoginAdminInfo;
import roomescape.auth.login.presentation.dto.LoginCheckResponse;
import roomescape.auth.login.presentation.dto.LoginRequest;
import roomescape.auth.login.presentation.dto.annotation.LoginAdmin;
import roomescape.auth.login.service.LoginService;

@RestController
@RequestMapping("/admin")
public class AdminLoginController {

    private final LoginService loginService;

    public AdminLoginController(LoginService loginService) {
        this.loginService = loginService;
    }

    @PostMapping("/login")
    public ResponseEntity<Void> login(@RequestBody LoginRequest request) {
        String token = loginService.createAdminToken(request);

        ResponseCookie cookie = ResponseCookie.from("token", token)
                .httpOnly(true)
                .path("/")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .build();
    }

    @GetMapping("/login/check")
    public ResponseEntity<LoginCheckResponse> checkLogin(@LoginAdmin final LoginAdminInfo info) {
        Admin admin = loginService.findByAdminId(info.id());
        return ResponseEntity.ok().body(new LoginCheckResponse(admin.getName()));
    }
}
