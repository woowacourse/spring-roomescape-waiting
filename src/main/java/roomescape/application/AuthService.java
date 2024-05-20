package roomescape.application;


import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;
import roomescape.dto.MemberResponse;
import roomescape.dto.TokenRequest;
import roomescape.dto.TokenResponse;
import roomescape.domain.member.Email;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberRepository;
import roomescape.exception.RoomescapeErrorCode;
import roomescape.exception.RoomescapeException;

@Service
public class AuthService {
    private final TokenManager tokenManager;
    private final MemberRepository memberRepository;

    public AuthService(TokenManager tokenManager, MemberRepository memberRepository) {
        this.tokenManager = tokenManager;
        this.memberRepository = memberRepository;
    }

    public TokenResponse createToken(TokenRequest tokenRequest) {
        String email = tokenRequest.email();
        Member member = getMemberByEmail(email);
        if (!member.isSamePassword(tokenRequest.password())) {
            throw new RoomescapeException(RoomescapeErrorCode.BAD_REQUEST, "로그인 회원 정보가 일치하지 않습니다.");
        }
        String payload = String.valueOf(member.getId());
        String accessToken = tokenManager.createToken(payload);
        return new TokenResponse(accessToken);
    }

    private Member getMemberByEmail(String email) {
        return memberRepository.findByEmail(new Email(email))
                .orElseThrow(() -> new RoomescapeException(RoomescapeErrorCode.NOT_FOUND_MEMBER,
                        String.format("존재하지 않는 회원입니다. 입력한 회원 email:%s", email)));
    }

    public MemberResponse findMemberByCookies(Cookie[] cookies) {
        String payload = tokenManager.getPayload(extractToken(cookies));
        long id = Long.parseLong(payload);
        return MemberResponse.from(getMemberById(id));
    }

    private String extractToken(Cookie[] cookies) {
        return tokenManager.extractToken(cookies);
    }

    private Member getMemberById(long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new RoomescapeException(RoomescapeErrorCode.NOT_FOUND_MEMBER,
                        String.format("존재하지 않는 회원입니다. 입력한 회원 id:%d", id)));
    }

    public void setToken(HttpServletResponse response, String accessToken) {
        tokenManager.setToken(response, accessToken);
    }
}
