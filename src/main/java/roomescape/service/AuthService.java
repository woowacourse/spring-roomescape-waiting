package roomescape.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.controller.dto.request.LoginRequest;
import roomescape.domain.member.Email;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberRepository;
import roomescape.security.TokenProvider;
import roomescape.service.dto.response.MemberResponse;

@Service
@Transactional(readOnly = true)
public class AuthService {

    private static final String WRONG_EMAIL_OR_PASSWORD_MESSAGE = "등록되지 않은 이메일이거나 비밀번호가 틀렸습니다.";

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;

    public AuthService(
            MemberRepository memberRepository,
            PasswordEncoder passwordEncoder,
            TokenProvider tokenProvider
    ) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
    }

    public String createToken(Long memberId) {
        return tokenProvider.createToken(memberId.toString());
    }

    public Long getMemberIdByToken(String token) {
        return tokenProvider.getMemberId(token);
    }

    public MemberResponse validatePassword(LoginRequest loginRequest) { // todo void, dto 분리
        Member member = getMember(loginRequest);
        validatePassword(loginRequest, member);
        return MemberResponse.from(member);
    }

    private Member getMember(LoginRequest loginRequest) {
        Email email = new Email(loginRequest.email());
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException(WRONG_EMAIL_OR_PASSWORD_MESSAGE));
    }

    private void validatePassword(LoginRequest loginRequest, Member member) {
        if (!passwordEncoder.matches(loginRequest.password(), member.getPassword())) {
            throw new IllegalArgumentException(WRONG_EMAIL_OR_PASSWORD_MESSAGE);
        }
    }
}
