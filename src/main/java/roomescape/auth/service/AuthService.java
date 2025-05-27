package roomescape.auth.service;

import org.springframework.stereotype.Service;
import roomescape.auth.dto.request.LoginRequest;
import roomescape.auth.dto.response.AuthenticatedUserResponse;
import roomescape.auth.infrastructure.JwtTokenProvider;
import roomescape.exception.LoginFailedException;
import roomescape.exception.MemberNotFoundException;
import roomescape.member.domain.Member;
import roomescape.member.infrastructure.MemberRepository;

@Service
public class AuthService {

    private final JwtTokenProvider tokenProvider;
    private final MemberRepository memberRepository;


    public AuthService(JwtTokenProvider jwtTokenProvider, MemberRepository memberRepository) {
        this.tokenProvider = jwtTokenProvider;
        this.memberRepository = memberRepository;
    }

    public String createToken(LoginRequest loginRequest) {
        Member member = memberRepository.findByEmail(loginRequest.email())
                .orElseThrow(LoginFailedException::new);
        member.validatePassword(loginRequest.password());

        return tokenProvider.createToken(String.valueOf(member.getId()), member.getRole());
    }

    public AuthenticatedUserResponse getAuthenticatedUser(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(MemberNotFoundException::new);
        return new AuthenticatedUserResponse(member.getName());
    }
}
