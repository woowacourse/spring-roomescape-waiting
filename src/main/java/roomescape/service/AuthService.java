package roomescape.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.member.Member;
import roomescape.dto.auth.LoginRequestDto;
import roomescape.exception.NotFoundException;
import roomescape.repository.JpaMemberRepository;
import roomescape.util.JwtTokenProvider;

@Service
public class AuthService {

    private final JpaMemberRepository memberRepository;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(JpaMemberRepository memberRepository, JwtTokenProvider jwtTokenProvider) {
        this.memberRepository = memberRepository;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Transactional(readOnly = true)
    public String publishLoginToken(LoginRequestDto loginRequestDto) {
        String email = loginRequestDto.email();
        String password = loginRequestDto.password();

        Member member = memberRepository.findByEmailAndPassword(email, password).orElseThrow(
                () -> new NotFoundException("[ERROR] 이메일이나 비밀번호가 올바르지 않습니다."));
        return jwtTokenProvider.createToken(member);
    }
}
