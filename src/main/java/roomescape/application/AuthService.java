package roomescape.application;


import jakarta.servlet.http.Cookie;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import roomescape.domain.member.Email;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberRepository;
import roomescape.dto.MemberResponse;
import roomescape.dto.TokenRequest;
import roomescape.dto.TokenResponse;
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
            throw new RoomescapeException(HttpStatus.NOT_FOUND,
                    String.format("존재하지 않는 회원입니다. 입력한 회원 email:%s", email));
        }
        String payload = String.valueOf(member.getId());
        String accessToken = tokenManager.createToken(payload);
        return new TokenResponse(accessToken);
    }

    private Member getMemberByEmail(String email) {
        return memberRepository.findByEmail(new Email(email))
                .orElseThrow(() -> new RoomescapeException(HttpStatus.NOT_FOUND,
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
                .orElseThrow(() -> new RoomescapeException(HttpStatus.NOT_FOUND,
                        String.format("존재하지 않는 회원입니다. 입력한 회원 id:%d", id)));
    }

    public Cookie addTokenToCookie(String accessToken) {
        return tokenManager.addTokenToCookie(accessToken);
    }

    public void validateToken(Cookie[] cookies) {
        String token = extractToken(cookies);
        tokenManager.validateExpiration(token);
    }
}
