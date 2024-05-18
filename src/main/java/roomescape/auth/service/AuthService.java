package roomescape.auth.service;

import org.springframework.stereotype.Service;
import roomescape.auth.controller.dto.request.LoginRequest;
import roomescape.auth.jwt.JwtTokenProvider;
import roomescape.member.domain.Email;
import roomescape.member.domain.Member;
import roomescape.member.repository.MemberRepository;

@Service
public class AuthService {

    private final JwtTokenProvider jwtTokenProvider;
    private final MemberRepository memberRepository;

    public AuthService(final JwtTokenProvider jwtTokenProvider, final MemberRepository memberRepository) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.memberRepository = memberRepository;
    }

    public String createToken(final LoginRequest loginRequest) {
        memberRepository.getByEmail(new Email(loginRequest.email()));
        return jwtTokenProvider.createToken(loginRequest.email());
    }

    public Member findMemberByToken(final String token) {
        String email = jwtTokenProvider.getPayload(token);
        return memberRepository.getByEmail(new Email(email));
    }
}
