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
    private final TokenProvider tokenProvider;
    private final TokenManager tokenManager;
    private final MemberRepository memberRepository;

    public AuthService(TokenProvider tokenProvider, TokenManager tokenManager,
                       MemberRepository memberRepository) {
        this.tokenProvider = tokenProvider;
        this.tokenManager = tokenManager;
        this.memberRepository = memberRepository;
    }

    public TokenResponse createToken(TokenRequest tokenRequest) {
        String email = tokenRequest.email();
        Member member = getMemberBy(email);
        if (!member.matchPassword(tokenRequest.password())) {
            throw new RoomescapeException(RoomescapeErrorCode.BAD_REQUEST, "로그인 회원 정보가 일치하지 않습니다.");
        }
        String payload = String.valueOf(member.getId());
        String accessToken = tokenProvider.createToken(payload);
        return new TokenResponse(accessToken);
    }

    public MemberResponse findMemberByToken(String token) {
        String payload = tokenProvider.getPayload(token);
        long id = Long.parseLong(payload);
        return MemberResponse.from(getMemberBy(id));
    }

    private Member getMemberBy(String email) {
        return memberRepository.findByEmail(new Email(email))
                .orElseThrow(() -> new RoomescapeException(RoomescapeErrorCode.NOT_FOUND_MEMBER,
                        String.format("존재하지 않는 회원입니다. 입력한 회원 email:%s", email)));
    }

    private Member getMemberBy(long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new RoomescapeException(RoomescapeErrorCode.NOT_FOUND_MEMBER,
                        String.format("존재하지 않는 회원입니다. 입력한 회원 id:%d", id)));
    }

    public String extractToken(Cookie[] cookies) {
        return tokenManager.extractToken(cookies);
    }

    public void setToken(HttpServletResponse response, String accessToken) {
        tokenManager.setToken(response, accessToken);
    }
}
