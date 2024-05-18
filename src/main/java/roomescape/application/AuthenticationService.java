package roomescape.application;


import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;
import roomescape.application.dto.MemberResponse;
import roomescape.application.dto.TokenRequest;
import roomescape.application.dto.TokenResponse;
import roomescape.domain.Email;
import roomescape.domain.Member;
import roomescape.domain.repository.MemberQueryRepository;
import roomescape.exception.RoomescapeErrorCode;
import roomescape.exception.RoomescapeException;

@Service
public class AuthenticationService {

    private final TokenProvider tokenProvider;
    private final TokenManager tokenManager;
    private final MemberQueryRepository memberQueryRepository;

    public AuthenticationService(TokenProvider tokenProvider, TokenManager tokenManager,
                                 MemberQueryRepository memberQueryRepository) {
        this.tokenProvider = tokenProvider;
        this.tokenManager = tokenManager;
        this.memberQueryRepository = memberQueryRepository;
    }

    public TokenResponse createToken(TokenRequest tokenRequest) {
        Email email = new Email(tokenRequest.email());
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

    private Member getMemberBy(Email email) {
        return memberQueryRepository.getByEmail(email);
    }

    private Member getMemberBy(Long id) {
        return memberQueryRepository.getById(id);
    }

    public String extractToken(Cookie[] cookies) {
        return tokenManager.extractToken(cookies);
    }

    public void setToken(HttpServletResponse response, String accessToken) {
        tokenManager.setToken(response, accessToken);
    }
}
