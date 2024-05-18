package roomescape.auth.service;

import org.springframework.stereotype.Service;
import roomescape.auth.dto.Accessor;
import roomescape.auth.dto.LoginCheckResponse;
import roomescape.auth.dto.LoginRequest;
import roomescape.auth.infrastructure.JwtTokenProvider;
import roomescape.auth.infrastructure.Token;
import roomescape.global.exception.NoSuchRecordException;
import roomescape.global.exception.auth.WrongPasswordException;
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

    public Token login(LoginRequest loginRequest) {
        Member foundMember = memberRepository.findByEmail(loginRequest.email())
                .orElseThrow(() -> new NoSuchRecordException("이메일: " + loginRequest.email() + " 해당하는 멤버를 찾을 수 없습니다"));

        if (!foundMember.isSamePassword(loginRequest.password())) {
            throw new WrongPasswordException("비밀번호가 틀렸습니다");
        }

        return jwtTokenProvider.createToken(foundMember);
    }

    public LoginCheckResponse checkLogin(Accessor accessor) {
        Member findMember = memberRepository.findById(accessor.id())
                .orElseThrow(() -> new NoSuchRecordException("id: " + accessor.id() + " 해당하는 회원을 찾을 수 없습니다"));
        return new LoginCheckResponse(findMember.getName());
    }
}
