package roomescape.auth.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.auth.dto.Accessor;
import roomescape.auth.dto.LoginCheckResponse;
import roomescape.auth.dto.LoginRequest;
import roomescape.auth.infrastructure.JwtTokenProvider;
import roomescape.auth.infrastructure.Token;
import roomescape.global.exception.AuthenticationException;
import roomescape.member.domain.Email;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRepository;

@Service
public class AuthService {

    private final MemberRepository memberRepository;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(MemberRepository memberRepository, JwtTokenProvider jwtTokenProvider) {
        this.memberRepository = memberRepository;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Transactional(readOnly = true)
    public Token login(LoginRequest loginRequest) {
        Email loginEmail = new Email(loginRequest.email());
        Member findMember = memberRepository.findByEmail(loginEmail)
                .orElseThrow(() -> new AuthenticationException("이메일: " + loginRequest.email() + " 해당하는 멤버를 찾을 수 없습니다"));

        if (!findMember.getPassword().equals(loginRequest.password())) {
            throw new AuthenticationException("비밀번호가 틀렸습니다");
        }

        return jwtTokenProvider.createToken(findMember);
    }

    @Transactional(readOnly = true)
    public LoginCheckResponse checkLogin(Accessor accessor) {
        Member findMember = memberRepository.findById(accessor.id())
                .orElseThrow(() -> new AuthenticationException("id: " + accessor.id() + " 해당하는 회원을 찾을 수 없습니다"));
        return new LoginCheckResponse(findMember.getName());
    }
}
