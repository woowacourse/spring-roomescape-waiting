package roomescape.member.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.member.controller.request.TokenLoginCreateRequest;
import roomescape.member.controller.response.MemberResponse;
import roomescape.member.controller.response.TokenLoginResponse;
import roomescape.member.resolver.LoginMember;
import roomescape.member.service.AutoService;

@RequestMapping("login")
@RestController
public class TokenLoginApiController {

    private final AutoService autoService;

    public TokenLoginApiController(AutoService autoService) {
        this.autoService = autoService;
    }

    @PostMapping
    public ResponseEntity<Void> tokenLogin(
            @RequestBody TokenLoginCreateRequest tokenLoginCreateRequest,
            HttpServletResponse response
    ){
        TokenLoginResponse tokenLoginResponse = autoService.tokenLogin(tokenLoginCreateRequest);
        Cookie cookie = new Cookie("token", tokenLoginResponse.tokenResponse());
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        response.addCookie(cookie);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/check")
    public ResponseEntity<MemberResponse> getMember(
            @LoginMember MemberResponse response
    ) {
        return ResponseEntity.ok().body(response);
    }
}
