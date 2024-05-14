package roomescape.controller.member;

import static roomescape.global.Constants.TOKEN_NAME;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import roomescape.controller.helper.AuthenticationPrincipal;
import roomescape.controller.helper.LoginMember;
import roomescape.global.CookieUtils;
import roomescape.service.MemberService;
import roomescape.service.dto.member.MemberCreateRequest;
import roomescape.service.dto.member.MemberLoginRequest;
import roomescape.service.dto.member.MemberResponse;

@RestController
public class MemberRestController {

    private final MemberService memberService;

    public MemberRestController(MemberService memberService) {
        this.memberService = memberService;
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/members/signup")
    public void signup(@Valid @RequestBody MemberCreateRequest request) {
        memberService.signup(request);
    }

    @PostMapping("/members/login")
    public void login(@Valid @RequestBody MemberLoginRequest request, HttpServletResponse response) {
        String token = memberService.login(request);
        CookieUtils.addCookie(response, TOKEN_NAME, token, 300);
    }

    @GetMapping("/members/login/check")
    public MemberResponse checkLogin(@AuthenticationPrincipal LoginMember loginMember) {
        return new MemberResponse(loginMember);
    }

    @PostMapping("/members/logout")
    public void logout(HttpServletResponse response) {
        CookieUtils.deleteCookie(response, TOKEN_NAME);
    }

    @GetMapping("/admin/members")
    public List<MemberResponse> findAllMembers() {
        return memberService.findAllMemberNames();
    }
}
