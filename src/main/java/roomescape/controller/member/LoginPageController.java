package roomescape.controller.member;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.dto.member.LoginRequestDto;
import roomescape.dto.member.LoginResponseDto;
import roomescape.service.member.MemberService;

@RestController
@RequestMapping("/login")
public class LoginPageController {

    private final MemberService memberService;

    public LoginPageController(MemberService memberService) {
        this.memberService = memberService;
    }

    @PostMapping
    public ResponseEntity<LoginResponseDto> login(@RequestBody LoginRequestDto loginRequestDto,
                                                  HttpServletResponse response) {
        String token = memberService.login(loginRequestDto);
        response.addCookie(new Cookie("token", token));

        return ResponseEntity.ok(new LoginResponseDto(token));
    }
}
