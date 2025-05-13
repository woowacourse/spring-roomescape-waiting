package roomescape.service;

import org.springframework.stereotype.Service;
import roomescape.domain.Member;
import roomescape.domain.repository.MemberRepository;
import roomescape.dto.request.LoginRequest;
import roomescape.dto.response.AuthenticatedUserResponse;
import roomescape.exception.LoginFailedException;
import roomescape.exception.MemberNotFoundException;
import roomescape.infrastructure.JwtTokenProvider;

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
