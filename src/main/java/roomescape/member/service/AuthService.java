package roomescape.member.service;

import org.springframework.stereotype.Service;
import roomescape.member.controller.request.TokenLoginCreateRequest;
import roomescape.member.controller.response.MemberResponse;
import roomescape.member.controller.response.TokenLoginResponse;
import roomescape.member.domain.Email;
import roomescape.member.domain.Member;
import roomescape.member.domain.Password;
import roomescape.member.infrastructure.JwtTokenProvider;

@Service
public class AuthService {
    private final JwtTokenProvider jwtTokenProvider;
    private final MemberRepository memberRepository;

    public AuthService(JwtTokenProvider jwtTokenProvider, MemberRepository memberRepository) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.memberRepository = memberRepository;
    }

    public TokenLoginResponse tokenLogin(TokenLoginCreateRequest tokenLoginCreateRequest) {
        Email email = new Email(tokenLoginCreateRequest.email());
        Password password = new Password(tokenLoginCreateRequest.password());

        if (!memberRepository.isExistUser(email.getEmail(), password.getPassword())) {
            throw new IllegalArgumentException("[ERROR] 등록되지 않은 회원입니다.");
        }

        String accessToken = jwtTokenProvider.createToken(tokenLoginCreateRequest.email());
        return new TokenLoginResponse(accessToken);
    }

    public MemberResponse findUserByToken(String token) {
        String payload = jwtTokenProvider.getPayload(token);
        Member member = memberRepository.findUserByEmail(payload);
        return new MemberResponse(member.getName(), member.getRole());
    }

}
