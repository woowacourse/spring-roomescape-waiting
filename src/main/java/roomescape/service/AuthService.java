package roomescape.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.member.Email;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberRepository;
import roomescape.security.authentication.AnonymousAuthentication;
import roomescape.security.authentication.Authentication;
import roomescape.security.authentication.DefaultAuthentication;
import roomescape.security.provider.TokenProvider;
import roomescape.service.dto.request.CreateTokenRequest;
import roomescape.service.dto.response.TokenResponse;

@Service
@Transactional(readOnly = true)
public class AuthService {

    private static final String WRONG_EMAIL_OR_PASSWORD_MESSAGE = "등록되지 않은 이메일이거나 비밀번호가 틀렸습니다.";

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;

    public AuthService(MemberRepository memberRepository,
                       PasswordEncoder passwordEncoder,
                       TokenProvider tokenProvider) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
    }

    public TokenResponse authenticateMember(CreateTokenRequest createTokenRequest) {
        Member member = getMember(createTokenRequest.email());
        validatePassword(createTokenRequest.password(), member);
        String token = tokenProvider.createToken(Long.toString(member.getId()));
        return new TokenResponse(token);
    }

    private Member getMember(String rawEmail) {
        Email email = new Email(rawEmail);
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException(WRONG_EMAIL_OR_PASSWORD_MESSAGE));
    }

    private void validatePassword(String password, Member member) {
        if (!passwordEncoder.matches(password, member.getPassword())) {
            throw new IllegalArgumentException(WRONG_EMAIL_OR_PASSWORD_MESSAGE);
        }
    }

    public Authentication createAuthentication(String token) {
        String subject = tokenProvider.extractSubject(token);
        long memberId = Long.parseLong(subject);
        return memberRepository.findById(memberId)
                .map(DefaultAuthentication::from)
                .orElseGet(AnonymousAuthentication::new);
    }
}
