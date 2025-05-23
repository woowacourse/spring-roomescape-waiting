package roomescape.auth.login.presentation;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.login.presentation.dto.LoginCheckResponse;
import roomescape.auth.login.presentation.dto.LoginMemberInfo;
import roomescape.auth.login.presentation.dto.LoginRequest;
import roomescape.auth.login.presentation.dto.annotation.LoginMember;
import roomescape.auth.login.service.LoginService;
import roomescape.member.domain.Member;

@RestController
public class MemberLoginController {

    private final LoginService loginService;

    public MemberLoginController(LoginService loginService) {
        this.loginService = loginService;
    }

    @PostMapping("/login")
    public ResponseEntity<Void> login(@RequestBody LoginRequest request) {
        String token = loginService.createMemberToken(request);

        ResponseCookie cookie = ResponseCookie.from("token", token)
                .httpOnly(true)
                .path("/")
                .maxAge(1800000)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .build();
    }

    @GetMapping("/login/check")
    public ResponseEntity<LoginCheckResponse> checkLogin(@LoginMember final LoginMemberInfo info) {
        Member member = loginService.findByMemberId(info.id());
        return ResponseEntity.ok().body(new LoginCheckResponse(member.getName().name()));
    }
}
