package roomescape.application.auth;

import jakarta.servlet.http.Cookie;
import org.springframework.stereotype.Service;
import roomescape.application.MemberService;
import roomescape.application.auth.dto.LoginRequest;
import roomescape.application.auth.dto.MemberIdDto;
import roomescape.application.dto.MemberServiceResponse;
import roomescape.domain.Role;
import roomescape.exception.AuthorizationException;
import roomescape.exception.NotFoundException;
import roomescape.infrastructure.jwt.JwtTokenProvider;

@Service
public class AuthService {

    private final MemberService memberService;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(MemberService memberService, JwtTokenProvider jwtTokenProvider) {
        this.memberService = memberService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public MemberServiceResponse getMemberById(Long id) {
        return memberService.getMemberById(id);
    }

    public Cookie createCookie(LoginRequest loginRequest) {
        String accessToken = createToken(loginRequest);

        Cookie cookie = new Cookie("token", accessToken);
        cookie.setMaxAge(3600000);
        cookie.setSecure(true);
        cookie.setHttpOnly(true);
        cookie.setPath("/");

        return cookie;
    }

    private String createToken(LoginRequest loginRequest) {
        try {
            MemberServiceResponse memberServiceResponse = memberService.getMemberBy(loginRequest.email(), loginRequest.password());
            return jwtTokenProvider.createToken(new MemberIdDto(memberServiceResponse.id()));
        } catch (NotFoundException e) {
            throw new AuthorizationException("이메일 또는 비밀번호가 일치하지 않습니다.");
        }
    }

    public boolean isAdminAuthorized(MemberIdDto memberIdDto) {
        MemberServiceResponse memberServiceResponse = memberService.getMemberById(memberIdDto.id());
        return memberServiceResponse.role() == Role.ADMIN;
    }
}
