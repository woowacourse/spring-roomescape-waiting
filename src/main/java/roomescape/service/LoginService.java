package roomescape.service;

import org.springframework.stereotype.Service;
import roomescape.domain.member.Member;
import roomescape.global.auth.JwtManager;
import roomescape.global.exception.AuthorizationException;
import roomescape.repository.MemberRepository;

@Service
public class LoginService {

    private final MemberRepository memberRepository;
    private final JwtManager jwtManager;

    public LoginService(MemberRepository memberRepository, JwtManager jwtManager) {
        this.memberRepository = memberRepository;
        this.jwtManager = jwtManager;
    }

    public String login(String email, String password) {
        Member member = memberRepository.findByEmailAndPassword(email, password)
            .orElseThrow(() -> new AuthorizationException("아이디 혹은 패스워드가 일치하지 않습니다."));

        return jwtManager.createToken(member);
    }
}
