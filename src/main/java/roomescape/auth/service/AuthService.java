package roomescape.auth.service;

import org.springframework.stereotype.Service;
import roomescape.auth.dto.LoginRequest;
import roomescape.auth.token.TokenProvider;
import roomescape.member.encoder.PasswordEncoder;
import roomescape.member.model.Member;
import roomescape.member.repository.MemberRepository;

import java.util.NoSuchElementException;

@Service
public class AuthService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;

    public AuthService(
            final MemberRepository memberRepository,
            final PasswordEncoder passwordEncoder,
            final TokenProvider tokenProvider
    ) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
    }

    public String login(final LoginRequest request) {
        final Member member = memberRepository.findByEmail(request.email())
                .orElseThrow(() -> new NoSuchElementException("해당 이메일 정보와 일치하는 회원 정보가 없습니다."));
        checkCredential(member, request.password());

        return tokenProvider.createToken(member.getId(), member.getRole());
    }

    private void checkCredential(final Member member, final String password) {
        if (!passwordEncoder.matches(password, member.getPassword().value())) {
            throw new IllegalArgumentException("일치하지 않는 비밀번호입니다.");
        }
    }

    public Member findAuthenticatedMember(final String accessToken) {
        final Long memberId = Long.parseLong(tokenProvider.getTokenClaims(accessToken).getSubject());
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new NoSuchElementException("해당 회원 아이디와 일치하는 회원 정보가 없습니다."));
    }
}
