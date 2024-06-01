package roomescape.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Member;
import roomescape.domain.MemberRepository;
import roomescape.dto.request.LogInRequest;
import roomescape.service.exception.AuthFailureCustomException;
import roomescape.util.JwtProvider;

@Service
public class AuthService {

    private final MemberRepository memberRepository;
    private final JwtProvider jwtProvider;

    public AuthService(MemberRepository memberRepository, JwtProvider jwtProvider) {
        this.memberRepository = memberRepository;
        this.jwtProvider = jwtProvider;
    }

    @Transactional(readOnly = true)
    public String logIn(LogInRequest logInRequest) {
        String email = logInRequest.email();
        String password = logInRequest.password();
        Member member = findMemberByEmailAndPassword(email, password);

        return jwtProvider.createToken(member);
    }

    private Member findMemberByEmailAndPassword(String email, String password) {
        return memberRepository.findByEmailAndPassword(email, password)
                .orElseThrow(() -> new AuthFailureCustomException("일치하는 이메일과 비밀번호가 없습니다."));
    }
}
