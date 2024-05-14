package roomescape.application;


import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;
import roomescape.application.dto.MemberResponse;
import roomescape.application.dto.TokenRequest;
import roomescape.application.dto.TokenResponse;
import roomescape.domain.Member;
import roomescape.domain.MemberQueryRepository;
import roomescape.exception.RoomescapeErrorCode;
import roomescape.exception.RoomescapeException;

@Service
public class AuthService {
    private final TokenProvider tokenProvider;
    private final TokenManager tokenManager;
    private final MemberQueryRepository memberQueryRepository;

    public AuthService(TokenProvider tokenProvider, TokenManager tokenManager, MemberQueryRepository memberQueryRepository) {
        this.tokenProvider = tokenProvider;
        this.tokenManager = tokenManager;
        this.memberQueryRepository = memberQueryRepository;
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
        return memberQueryRepository.findByEmail(email)
                .orElseThrow(() -> new RoomescapeException(RoomescapeErrorCode.NOT_FOUND_MEMBER,
                        String.format("존재하지 않는 회원입니다. 입력한 회원 email:%s", email)));
    }

    private Member getMemberBy(long id) {
        return memberQueryRepository.findById(id)
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
