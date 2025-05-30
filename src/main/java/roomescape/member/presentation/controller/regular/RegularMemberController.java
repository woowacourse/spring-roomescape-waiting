package roomescape.member.presentation.controller.regular;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.common.security.annotation.RequireRole;
import roomescape.common.security.dto.request.MemberInfo;
import roomescape.common.security.dto.response.CheckLoginResponse;
import roomescape.common.security.infrastructure.CookieManager;
import roomescape.member.application.MemberApplicationService;
import roomescape.member.domain.MemberRole;

@RestController
public class RegularMemberController {

    private static final String TOKEN = "token";

    private final MemberApplicationService memberApplicationService;
    private final CookieManager cookieManager;

    public RegularMemberController(final MemberApplicationService memberApplicationService,
                                   final CookieManager cookieManager) {
        this.memberApplicationService = memberApplicationService;
        this.cookieManager = cookieManager;
    }

    @RequireRole(MemberRole.REGULAR)
    @GetMapping("/login/check")
    public ResponseEntity<CheckLoginResponse> checkLogin(final MemberInfo memberInfo) {
        return ResponseEntity.ok(CheckLoginResponse.from(memberApplicationService.getById(memberInfo.id())));
    }

    @RequireRole(MemberRole.REGULAR)
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(final HttpServletResponse httpServletResponse) {
        cookieManager.deleteCookie(httpServletResponse, TOKEN);
        return ResponseEntity.noContent().build();
    }
}
